package com.lorepo.icplayer.client.module.imagesource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.lorepo.icf.properties.IBooleanProperty;
import com.lorepo.icf.properties.IImageProperty;
import com.lorepo.icf.properties.IProperty;
import com.lorepo.icf.utils.StringUtils;
import com.lorepo.icf.utils.XMLUtils;
import com.lorepo.icf.utils.i18n.DictionaryWrapper;
import com.lorepo.icplayer.client.module.BasicModuleModel;

public class ImageSourceModule extends BasicModuleModel {

	private String imagePath = "";
	private boolean removable = true;
	private boolean isDisabled = false;
	private String altText = "";

	public ImageSourceModule() {
		super("Image source", DictionaryWrapper.get("image_source_module"));
		
		addPropertyImage();
		addPropertyIsDisabled();
		addPropertyRemovable();
		addPropertyAltText();
	}

	public String getUrl() {
		if (imagePath.isEmpty()) {
			return GWT.getModuleBaseURL() + "media/no_image.gif";
		}
		
		if (imagePath.startsWith("http") || imagePath.startsWith("/")) {
			return imagePath;
		}
		
		return this.baseURL + imagePath;
	}

	@Override
	protected void parseModuleNode(Element node) {
		NodeList nodes = node.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node childNode = nodes.item(i);
			if (childNode instanceof Element) {
				if (childNode.getNodeName().compareTo("image") == 0 && childNode instanceof Element) {
					Element childElement = (Element) childNode;
					imagePath = StringUtils.unescapeXML(childElement.getAttribute("src"));
					removable = XMLUtils.getAttributeAsBoolean((Element)childNode, "removable", true);
					isDisabled = XMLUtils.getAttributeAsBoolean((Element)childNode, "isDisabled", false);
					altText = XMLUtils.getAttributeAsString(childElement, "altText");
				}
			}
		}
	}

	/**
	 * Convert module into XML
	 */
	@Override
	public String toXML() {
		String removableString = removable ? "True":"False";
		
		Element imageSourceModule = XMLUtils.createElement("imageSourceModule");
		this.setBaseXMLAttributes(imageSourceModule);
		imageSourceModule.appendChild(this.getLayoutsXML());

		Element image = XMLUtils.createElement("image");
		image.setAttribute("src", StringUtils.escapeHTML(imagePath));
		image.setAttribute("removable", removableString);
		image.setAttribute("isDisabled", Boolean.toString(this.isDisabled));
		image.setAttribute("altText", this.altText);
		imageSourceModule.appendChild(image);

		return imageSourceModule.toString();

	}
	
	private void addPropertyIsDisabled() {
		
		IProperty property = new IBooleanProperty() {
			
			@Override
			public void setValue(String newValue) {
				boolean value = (newValue.compareToIgnoreCase("true") == 0); 
				
				if (value!= isDisabled) {
					isDisabled = value;
					sendPropertyChangedEvent(this);
				}
			}
			
			@Override
			public String getValue() {
				return isDisabled ? "True" : "False";
			}
			
			@Override
			public String getName() {
				return DictionaryWrapper.get("image_source_is_disabled");
			}

			@Override
			public String getDisplayName() {
				return DictionaryWrapper.get("image_source_is_disabled");
			}

			@Override
			public boolean isDefault() {
				return false;
			}

		};
		
		addProperty(property);	
	}
	
	public boolean isDisabled() {
		return isDisabled;
	}
	
	private void addPropertyImage() {

		IProperty property = new IImageProperty() {
				
			@Override
			public void setValue(String newValue) {
				imagePath = newValue;
				sendPropertyChangedEvent(this);
			}
			
			@Override
			public String getValue() {
				return imagePath;
			}
			
			@Override
			public String getName() {
				return DictionaryWrapper.get("image_source_image");
			}

			@Override
			public String getDisplayName() {
				return DictionaryWrapper.get("image_source_image");
			}

			@Override
			public boolean isDefault() {
				return true;
			}
		};
		
		addProperty(property);
	}
	
	protected boolean isRemovable() {
		return removable;
	}
	
	private void addPropertyRemovable() {

		IProperty property = new IBooleanProperty() {
				
			@Override
			public void setValue(String newValue) {
				boolean value = (newValue.compareToIgnoreCase("true") == 0); 
				
				if(value!= removable){
					removable = value;
					sendPropertyChangedEvent(this);
				}
			}
			
			@Override
			public String getValue() {
				return removable ? "True" : "False";
			}
			
			@Override
			public String getName() {
				return DictionaryWrapper.get("image_source_removable");
			}

			@Override
			public String getDisplayName() {
				return DictionaryWrapper.get("image_source_removable");
			}

			@Override
			public boolean isDefault() {
				return false;
			}

		};
		
		addProperty(property);
	}

	private void addPropertyAltText() {
		IProperty property = new IProperty() {

			@Override
			public void setValue(String newValue) {
				altText = newValue;
				sendPropertyChangedEvent(this);
			}

			@Override
			public String getValue() {
				return altText;
			}

			@Override
			public String getName() {
				return DictionaryWrapper.get("image_source_alt_text");
			}

			@Override
			public String getDisplayName() {
				return DictionaryWrapper.get("image_source_alt_text");
			}

			@Override
			public boolean isDefault() {
				return false;
			}
		};

		addProperty(property);
	}

	public String getAlttext() {
		return altText;
	}

}
