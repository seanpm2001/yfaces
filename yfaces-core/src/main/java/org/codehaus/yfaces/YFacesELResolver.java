/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.yfaces;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Map;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.codehaus.yfaces.component.YFrame;
import org.codehaus.yfaces.component.YModel;
import org.codehaus.yfaces.component.YModelBinding;
import org.codehaus.yfaces.context.REQUEST_PHASE;
import org.codehaus.yfaces.context.YPageContext;
import org.codehaus.yfaces.context.YRequestContextImpl;

/**
 * A custom {@link ELResolver} implementation which handles {@link YModelBinding} and {@link YFrame}
 * instances.Whenever a resolved value leads into one of these instances some pre- pr
 * post-processing is done.
 * <ul>
 * <li> {@link YModelBinding}: automatically resolve it to {@link YModelBinding#getValue()} except
 * {@link YFacesELContext#isResolveYComponentBinding()} returns false</li>
 * <li> {@link YFrame}: notify current {@link YPageContext}</li>
 * </ul>
 * <p>
 * This resolver can't be element of the {@link ELResolver} chain but actually is a wrapper about
 * the standard resolver returned by the underlying JSF implementation.
 * <p>
 * 
 * @see YFacesApplication
 * @see YFacesELContextListener
 * @see YFacesELContext
 * 
 * @author Denny Strietzbaum
 * 
 */
public class YFacesELResolver extends ELResolver {

	private static final String ADD_FRAME_THRESHOLD = YFacesELResolver.class.getName() + "_addFrame";

	private static final Logger log = Logger.getLogger(YFacesELResolver.class);

	private ELResolver resolver = null;

	/**
	 * Constructor.<br/>
	 * The passed resolver should be the {@link CompositeELResolver} created by the JSF Framework for
	 * the {@link Application} instance. <br/>
	 * 
	 * @param resolver
	 */
	public YFacesELResolver(final ELResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Returns the original {@link ELResolver}.<br/>
	 * Generally this is the {@link CompositeELResolver} created by JSF for the {@link Application}
	 * instance. <br/>
	 * 
	 * @return the source {@link ELResolver}
	 */
	public ELResolver getSourceResolver() {
		return this.resolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ELResolver#getValue(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object getValue(final ELContext context, Object base, final Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {

		// when base is a YComponentBinding (Components within components;
		// component templates) then resolve to its value
		if (base instanceof YModelBinding) {
			base = ((YModelBinding<?>) base).getValue();
		}

		// delegate to original resolver
		Object result = this.resolver.getValue(context, base, property);

		// ... when value is a Frame: notify current YPage
		if (result instanceof YFrame) {
			this.addFrameToPageContext(context, (YFrame) result);
		}

		// ... when value is a YComponentBinding and resolving is enabled,
		// resolve value to YComponentBindings value
		if (getYContext(context).isResolveYComponentBinding() && result instanceof YModelBinding) {
			result = ((YModelBinding<?>) result).getValue();
		}

		//		if (result instanceof YModel) {
		//			log.debug("");
		//		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ELResolver#setValue(javax.el.ELContext, java.lang.Object, java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void setValue(final ELContext context, final Object base, final Object property,
			final Object value) throws NullPointerException, PropertyNotFoundException,
			PropertyNotWritableException, ELException {

		final Class<?> type = this.resolver.getType(context, base, property);

		//special handling in case of YComponentBinding
		if (YModelBinding.class.equals(type)) {
			final boolean resolveBinding = getYContext(context).isResolveYComponentBinding();
			if (resolveBinding) {
				final YModelBinding binding = (YModelBinding) this.resolver.getValue(context, base,
						property);
				binding.setValue((YModel) value);
			} else {
				this.resolver.setValue(context, base, property, value);
			}
		} else {
			this.resolver.setValue(context, base, property, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ELResolver#isReadOnly(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean isReadOnly(final ELContext context, final Object base, final Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {
		final boolean result = false;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ELResolver#getType(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Class<?> getType(final ELContext context, final Object base, final Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {
		// delegate to original resolver
		Class<?> result = this.resolver.getType(context, base, property);

		// when enabled, resolve YComponentBinding to YComponent
		if (getYContext(context).isResolveYComponentBinding() && YModelBinding.class.equals(result)) {
			result = YModel.class;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ELResolver#getCommonPropertyType(javax.el.ELContext, java.lang.Object)
	 */
	@Override
	public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
		return this.resolver.getCommonPropertyType(context, base);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ELResolver#getFeatureDescriptors(javax.el.ELContext, java.lang.Object)
	 */
	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context,
			final Object base) {
		return this.resolver.getFeatureDescriptors(context, base);
	}

	/**
	 * Extracts the {@link YFacesELContext} from passed {@link ELContext}
	 * 
	 * @param context
	 *          {@link ELContext}
	 * @return {@link YFacesELContext}
	 * 
	 * @see YFacesELContextListener
	 */
	private YFacesELContext getYContext(final ELContext context) {
		return (YFacesELContext) context.getContext(YFacesELContext.class);
	}

	/**
	 * Adds the frame to current {@link YPageContext} when necessary
	 * 
	 * @param frame
	 *          frame to add
	 * 
	 */
	private void addFrameToPageContext(final ELContext elCtx, final YFrame frame) {
		// frames are getting added when:
		// a) method is get
		// b) method is post and START_REQUEST phase has finished (nothing is done
		//    hen the Frame was requested from within an action/actionlistener)

		// a map holds a threshold two reduce unnecessary operations 
		// (context resolving is done multiple times within one request)  
		final Map<String, Object> m = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestMap();
		boolean threshold = m.containsKey(ADD_FRAME_THRESHOLD);
		if (!threshold) {
			final YRequestContextImpl yctx = (YRequestContextImpl) YFaces.getRequestContext();
			final boolean isPostback = yctx.isPostback();
			final boolean isStartRequest = yctx.getRequestPhase().equals(REQUEST_PHASE.START_REQUEST);

			if (!isPostback || !isStartRequest) {
				m.put(ADD_FRAME_THRESHOLD, threshold = Boolean.TRUE);
			}
		}

		// adding a frame more than one times doesn't matter; it's just ignored
		if (threshold) {
			YFaces.getRequestContext().getPageContext().addFrame(frame);
		}
	}

}