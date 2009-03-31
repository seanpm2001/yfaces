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

package de.hybris.yfaces.context;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * A context object whose scope and lifetime is bound to {@link HttpSession}.
 * 
 * @author Denny.Strietzbaum
 */
public class YSessionContext {

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private YConversationContext conversationCtx = null;

	public YSessionContext() {
		this.conversationCtx = new YConversationContext(null);
	}

	/**
	 * Custom attributes for free usage.
	 * 
	 * @return {@link Map}
	 */
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	public YConversationContext getConversationContext() {
		return this.conversationCtx;
	}

	/**
	 * Gets invoked for incoming POST requests (or GET as flashback). But only when request goes to
	 * same page or to a temporary inactive one (previous conversation page).Iterates over available
	 * {@link YConversationContext} instances and invokes {@link YConversationContext#update()} on
	 * each one
	 */
	protected void update() {
		conversationCtx.update();
	}

}
