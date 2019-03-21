/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.http;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;

import flex.messaging.FlexContext;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Output;
import flex.messaging.io.amf.AmfTrace;

/**
 * Spring-MVC {@link View} that renders AMF content by serializing the model for the current request using
 * BlazeDS's AMF serialization/deserialization APIs.
 *
 * <p>By default, the entire contents of the model map (with the exception of framework-specific classes) will be
 * encoded as AMF. For cases where the contents of the map need to be filtered, users may specify a specific set of
 * model attributes to encode via the {@link #setRenderedAttributes(Set) renderedAttributes} property.
 *
 * @author Jeremy Grelle
 */
public class AmfView extends AbstractView {

    public static final String DEFAULT_CONTENT_TYPE = "application/x-amf";

    private static final Log log = LogFactory.getLog(AmfView.class);
    
    private Set<String> renderedAttributes;

    private boolean disableCaching = true;
    
    public AmfView() {
        setContentType(DEFAULT_CONTENT_TYPE);
    }
    
    /**
     * Returns the attributes in the model that should be rendered by this view.
     */
    public Set<String> getRenderedAttributes() {
        return renderedAttributes;
    }

    /**
     * Sets the attributes in the model that should be rendered by this view. When set, all other model attributes will be
     * ignored.
     */
    public void setRenderedAttributes(Set<String> renderedAttributes) {
        this.renderedAttributes = renderedAttributes;
    }

    /**
     * Disables caching of the generated AMF response.
     *
     * <p>Default is {@code true}, which will prevent the client from caching the generated AMF response.
     */
    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(getContentType());
        response.setCharacterEncoding("UTF-8");
        if (disableCaching) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
            response.addDateHeader("Expires", 1L);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object value = filterModel(model);
        
        try {
        
            AmfTrace trace = null;
            if (log.isDebugEnabled()) {
                trace = new AmfTrace();
            }
            
            ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
            SerializationContext context = new SerializationContext();
            Amf3Output out = new Amf3Output(context);
            if (trace != null) {
            	out.setDebugTrace(trace);
            }
            out.setOutputStream(outBuffer);
            out.writeObject(value);
            out.flush();
            
            outBuffer.flush();
            
            response.setContentLength(outBuffer.size());
            outBuffer.writeTo(response.getOutputStream());
            
            if (log.isDebugEnabled()) {
                log.debug("Wrote AMF message:\n" + trace);
            }
        } finally {
            FlexContext.clearThreadLocalObjects();
            SerializationContext.clearThreadLocalObjects();
        }
    }
    
    /**
     * Filters out undesired attributes from the given model. The return value can be either another {@link Map}, or a
     * single value object.  If only a single attribute is present in the model map, that value will be returned instead 
     * of the full map.
     *
     * <p>Default implementation removes {@link BindingResult} instances and entries not included in the {@link
     * #setRenderedAttributes(Set) renderedAttributes} property.
     *
     * @param model the model, as passed on to {@link #renderMergedOutputModel}
     * @return the object to be rendered
     */
    protected Object filterModel(Map<String, Object> model) {
        Map<String, Object> result = new HashMap<String, Object>(model.size());
        Set<String> renderedAttributes =
                !CollectionUtils.isEmpty(this.renderedAttributes) ? this.renderedAttributes : model.keySet();
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            if (!(entry.getValue() instanceof BindingResult) && renderedAttributes.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        if (result.size() == 1) {
        	return result.values().iterator().next();
        } else {
        	return result;
        }
    }
}
