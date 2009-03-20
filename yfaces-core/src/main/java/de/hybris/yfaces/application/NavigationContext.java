/*
 * Copyright 2008 the original author or authors.
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
package de.hybris.yfaces.application;

import java.util.Collection;
import java.util.Map;

import de.hybris.yfaces.component.YComponent;
import de.hybris.yfaces.component.YFrame;

/**
 * The NavigationContext manages navigation and state through multiple pages.<br/>
 * {@link NavigationContext#getCurrentPage()} always returns the current visible
 * {@link YPageContext}.<br/>
 * The {@link NavigationContext} checks with each request whether the new requested
 * {@link YPageContext} is part of this {@link NavigationContext}.<br/>
 * <br/>
 * If so, the {@link YPageContext} gets added to the contexts page queue, otherwise whole context
 * information are reseted and the {@link YPageContext} queue gets cleared with the new YPage as
 * first element. <br/>
 * <br/>
 * This context evaluates each request and decides whether it is a valid NavigationRequest or not.<br/>
 * A valid NavigationRequest is present when the requested Page is:<br/>
 * - the current shown page<br/>
 * - a previously, context managed page<br/>
 * - the configured next page via {@link #getOrCreateNextPage()}<br/>
 * AND <br/>
 * The request was a POST or a GET but with enabled flash flag. <br/>
 * 
 * @author Denny.Strietzbaum
 */
public abstract class NavigationContext {

	/**
	 * Returns a context related map of attributes.<br/>
	 * Use this map to hold conversation/wizard like Attributes.<br/>
	 * <br/>
	 * 
	 * @return Attributes bound to this context.
	 */
	public abstract Map<String, Object> getAttributes();

	/**
	 * Returns the current displayed Page.<br/>
	 * May or may not have one ore more previous pages<br/>
	 * (depends on whether {@link #getOrCreateNextPage()} was called before current request)<br/>
	 * <br/>
	 * 
	 * @return the current {@link YPageContext}
	 */
	public abstract YPageContext getCurrentPage();

	/**
	 * Returns the {@link YPageContext} which becomes the current one with next request.<br/>
	 * Calling this method first lazily creates a new instance.<br/>
	 * Every other call returns the same instance as long as the current request is processed.<br/>
	 * When a new requests starts this instance gets added to the stack of available context pages.<br/>
	 * <br/>
	 * When this method isn't called the context is reseted with the next request.
	 * 
	 * @return {@link YPageContext}
	 */
	public abstract YPageContext getOrCreateNextPage();

	/**
	 * Starts updating this context.<br/>
	 * The default update process is:<br/>
	 * For each {@link YPageContext}, call {@link YPageContext#update(UserSessionPropertyChangeLog)}<br/>
	 * For each {@link YFrame} of current update {@link YPageContext} call
	 * {@link YFrame#update(UserSessionPropertyChangeLog)}<br/>
	 * For each {@link YComponent} of current update {@link YFrame} call
	 * {@link YComponent#update(UserSessionPropertyChangeLog)}<br/>
	 * <br/>
	 * An update is invoked for:<br/>
	 * - a Navigationrequest which requests the current Page<br/>
	 * - a Navigationrequest which requests a previous Page<br/>
	 * <br/>
	 * An update is not invoked for:<br/>
	 * - a Navigationrequest which requests a new Page<br/>
	 * - any other requests (non valid Navigationrequests)<br/>
	 * <br/>
	 */
	public abstract void update();

	public abstract Collection<YPageContext> getAllPages();

}
