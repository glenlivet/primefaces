/*
 * Copyright 2009-2013 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.component.commandbutton;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import org.primefaces.context.RequestContext;

import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.CSVBuilder;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class CommandButtonRenderer extends CoreRenderer {

    @Override
	public void decode(FacesContext context, UIComponent component) {
        CommandButton button = (CommandButton) component;
        if(button.isDisabled()) {
            return;
        }
        
		String param = component.getClientId(context);
		if(context.getExternalContext().getRequestParameterMap().containsKey(param)) {
			component.queueEvent(new ActionEvent(component));
		}
        
        decodeBehaviors(context, component);
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		CommandButton button = (CommandButton) component;
		
		encodeMarkup(context, button);
		encodeScript(context, button);
	}
	
	protected void encodeMarkup(FacesContext context, CommandButton button) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = button.getClientId(context);
		String type = button.getType();
        Object value = button.getValue();
        String icon = button.resolveIcon();
        RequestContext requestContext = RequestContext.getCurrentInstance();
        boolean csvEnabled = requestContext.getApplicationContext().getConfig().isClientSideValidationEnabled()&&button.isValidateClient();
        
        StringBuilder onclick = new StringBuilder();
        if(button.getOnclick() != null) {
            onclick.append(button.getOnclick()).append(";");
        }
        
        String onclickBehaviors = getEventBehaviors(context, button, "click");
        if(onclickBehaviors != null) {
            onclick.append(onclickBehaviors);
        }

		writer.startElement("button", button);
		writer.writeAttribute("id", clientId, "id");
		writer.writeAttribute("name", clientId, "name");
        writer.writeAttribute("class", button.resolveStyleClass(), "styleClass");

		if(!type.equals("reset") && !type.equals("button")) {
            String request;
            boolean ajax = button.isAjax();
			
            if(ajax) {
                 request = buildAjaxRequest(context, button, null);
            }
            else {
                UIComponent form = ComponentUtils.findParentForm(context, button);
                if(form == null) {
                    throw new FacesException("CommandButton : \"" + clientId + "\" must be inside a form element");
                }
                
                request = buildNonAjaxRequest(context, button, form, null, false);
            }
            
            if(csvEnabled) {
                CSVBuilder csvb = requestContext.getCSVBuilder();
                request = csvb.init().source("this").ajax(ajax).process(button, button.getProcess()).command(request).build();
            }
			
            onclick.append(request);
		}

		if(onclick.length() > 0) {
            if(button.requiresConfirmation()) {
                writer.writeAttribute("data-pfconfirmcommand", onclick.toString(), null);
                writer.writeAttribute("onclick", button.getConfirmationScript(), "onclick");
            }
            else
                writer.writeAttribute("onclick", onclick.toString(), "onclick");
		}
		
		renderPassThruAttributes(context, button, HTML.BUTTON_ATTRS, HTML.CLICK_EVENT);

        if(button.isDisabled()) writer.writeAttribute("disabled", "disabled", "disabled");
        if(button.isReadonly()) writer.writeAttribute("readonly", "readonly", "readonly");
		
        //icon
        if(icon != null && !icon.trim().equals("")) {
            String defaultIconClass = button.getIconPos().equals("left") ? HTML.BUTTON_LEFT_ICON_CLASS : HTML.BUTTON_RIGHT_ICON_CLASS; 
            String iconClass = defaultIconClass + " " + icon;
            
            writer.startElement("span", null);
            writer.writeAttribute("class", iconClass, null);
            writer.endElement("span");
        }
        
        //text
        writer.startElement("span", null);
        writer.writeAttribute("class", HTML.BUTTON_TEXT_CLASS, null);
        
        if(value == null) {
            writer.write("ui-button");
        }
        else {
            if(button.isEscape())
                writer.writeText(value, "value");
            else
                writer.write(value.toString());
        }
        
        writer.endElement("span");
			
		writer.endElement("button");
	}
	
	protected void encodeScript(FacesContext context, CommandButton button) throws IOException {
		String clientId = button.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("CommandButton", button.resolveWidgetVar(), clientId);
        
		encodeClientBehaviors(context, button);
        
        wb.finish();
	}
}