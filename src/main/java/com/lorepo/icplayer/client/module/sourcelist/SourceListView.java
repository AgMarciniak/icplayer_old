package com.lorepo.icplayer.client.module.sourcelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.lorepo.icf.utils.JavaScriptUtils;
import com.lorepo.icf.utils.StringUtils;
import com.lorepo.icf.utils.TextToSpeechVoice;
import com.lorepo.icplayer.client.framework.module.StyleUtils;
import com.lorepo.icplayer.client.module.IWCAG;
import com.lorepo.icplayer.client.module.IWCAGModuleView;
import com.lorepo.icplayer.client.module.sourcelist.SourceListPresenter.IDisplay;
import com.lorepo.icplayer.client.page.PageController;
import com.lorepo.icplayer.client.utils.DOMUtils;
import com.lorepo.icplayer.client.utils.MathJax;


public class SourceListView extends FlowPanel implements IDisplay, IWCAG, IWCAGModuleView {

	private static final String SELECTED_STYLE = "ic_sourceListItem-selected";
	private final SourceListModule module;
	private final HashMap<String, Label> labels = new HashMap<String, Label>();
	private IViewListener listener;
	private boolean isDragged = false;
	private boolean isTouchSupported = false;
	private boolean isPreview = false;
	private SourceListPresenter presenter = null;
	private Label labelToRemove = null;
	private String idOfLabelToRemove = null;
	private int currentLabel = 0;
	private ArrayList <String> labelsIds = new ArrayList <String>();
	private PageController pageController;
	private boolean isWCAGOn = false;

	public SourceListView(SourceListModule module, boolean isPreview){
		this.module = module;
		createUI(isPreview);
	}

	private void createUI(boolean isPreview) {
		this.isPreview = isPreview;
		setStyleName(module.getStyleClass().isEmpty() ? "ic_sourceList" : module.getStyleClass());
		StyleUtils.applyInlineStyle(this, module);
		if (!isPreview) {
			setVisible(module.isVisible());
		}
		getElement().setId(module.getId());
		
		getElement().setAttribute("lang", this.module.getLangAttribute());
	}

	private void fireClickEvent(String id) {
		if(!isDragged && listener != null){
			listener.onItemCliked(id);
		}
	}

	private void itemDragged(String id) {
		if(listener != null){
			listener.onItemDragged(id);
		}
	}

	@Override
	public void setDragMode() {
		isDragged = true;
	}

	@Override
	public void unsetDragMode() {
		isDragged = false;
	
		if (this.labelToRemove != null && this.idOfLabelToRemove != null) {
			this.removeItem(this.idOfLabelToRemove);
			this.remove(this.labelToRemove);
			this.labelToRemove = null;
			this.idOfLabelToRemove = null;
		}
	}

	@Override
	public void selectItem(String id) {
		Label label = labels.get(id);
		if (label != null) {
			label.addStyleName(SELECTED_STYLE);
		}

		this.speak(TextToSpeechVoice.create(this.module.getSpeechTextItem(0), ""), TextToSpeechVoice.create());
	}

	@Override
	public void addListener(IViewListener l) {
		this.listener = l;
	}

	@Override
	public void addItem(final String id, String item, boolean callMathJax) {

		final HTML label = new HTML(StringUtils.markup2html(item));
		label.setStyleName("ic_sourceListItem");

		if (module.isTabindexEnabled()) {
			label.getElement().setTabIndex(0);
		}

		if(module.isVertical()){
			DOMUtils.applyInlineStyle(label.getElement(), "display: block;");
		} else {
			DOMUtils.applyInlineStyle(label.getElement(), "display: inline-block;white-space: nowrap;");
		}
		labels.put(id, label);
		labelsIds.add(id);
		add(label);
		if (callMathJax) {
			refreshMath(label.getElement());
		}

		label.addTouchEndHandler(new TouchEndHandler() {

			@Override
			public void onTouchEnd(TouchEndEvent event) {
				isTouchSupported = true;
				fireClickEvent(id);
			}

		});

		label.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				event.preventDefault();
			}

		});

		label.addMouseUpHandler(new MouseUpHandler() {

			@Override
			public void onMouseUp(MouseUpEvent event) {
				if (!isTouchSupported) {
					fireClickEvent(id);
				}
			}
		});
		label.addDragStartHandler(new DragStartHandler() {

			@Override
			public void onDragStart(DragStartEvent event) {
				itemDragged(id);
			}
		});

        if(!isPreview){
    		JavaScriptUtils.makeDraggable(label.getElement(), presenter.getAsJavaScript());
        }
	}

	@Override
	public void setPresenter(SourceListPresenter p) {
		presenter = p;
	}

	@Override
	public Element getItem(String id) {
		return labels.get(id).getElement();
	}

	@Override
	public Set<String> getCurrentLabels() {
		return labels.keySet();
	}


	@Override
	public void removeItem(String id) {
		Label label = labels.get(id);
		if (label != null) {
			if (isDragged) {
				labelToRemove = label;
				idOfLabelToRemove = id;
			} else {
				if (label.getStyleName().contains("keyboard_navigation_active_element")){
					unMarkCurrentItem();
					labelsIds.remove(id);
					remove(label);
					if (labelsIds.size() > 0) {
						if (currentLabel >= labelsIds.size()) {
							currentLabel = 0;
						}
					markCurrentItem();
					}
					
				} else {
					labelsIds.remove(id);
					currentLabel = 0;
					remove(label);
				}
			}
		}
	}

	@Override
	public void deselectItem(String id, boolean read) {
		Label label = labels.get(id);
		if (label != null) {
			label.removeStyleName(SELECTED_STYLE);
			this.speak(TextToSpeechVoice.create(this.module.getSpeechTextItem(1), ""), TextToSpeechVoice.create());
		}

		if (module.isVertical()) {
			DOMUtils.applyInlineStyle(label.getElement(), "display: block; position: relative");
		} else {
			DOMUtils.applyInlineStyle(label.getElement(), "display: inline-block; white-space: nowrap; position: relative");
		}
	}

	@Override
	public void showItem(String id) {
		Label label = labels.get(id);
		if (label != null) {
			label.setVisible(true);
		}
	}

	@Override
	public void hideItem(String id) {
		Label label = labels.get(id);
		if (label != null) {
			label.setVisible(false);
		}
	}

	@Override
	public void removeAll() {
		labels.clear();
		clear();
	}

	public void refreshMath(Element element) {
		MathJax.refreshMathJax(element);
	}

	@Override
	public void show() {
		setVisible(true);
		refreshMath(getElement());
	}

	@Override
	public void hide() {
		setVisible(false);
		refreshMath(getElement());
	}

	@Override
	public native void connectDOMNodeRemovedEvent (String id) /*-{
		var $addon = $wnd.$(".ic_page [id='" + id + "']"),
			addon = $addon[0];

		function onDOMNodeRemoved (event) {
			var $draggableElements;

			if (event.target.getAttribute && event.target.getAttribute("class") && event.target.getAttribute("class").split(" ").indexOf("ui-draggable") !== -1) {
				$wnd.$(event.target).draggable("destroy");
				return;
			}
			else if (event.target !== addon) {
				return;
			}

			addon.removeEventListener("DOMNodeRemoved", onDOMNodeRemoved);

			$draggableElements = $addon.find(".ui-draggable");

			$draggableElements.draggable("destroy");

			$draggableElements = null;
			addon = null;
			$addon = null;
		}

	    if (addon && addon.addEventListener) {
		    addon.addEventListener("DOMNodeRemoved", onDOMNodeRemoved);
	    } else {
	        $addon = null;
	        addon = null;
	    }
	}-*/;

	@Override
	public String getName() {
		return "SourceList";
	}

	private void unMarkCurrentItem(){
		Label current = labels.get(labelsIds.get(currentLabel));
		current.removeStyleName("keyboard_navigation_active_element");
	}
	
	private void markCurrentItem(){
		Label current = labels.get(labelsIds.get(currentLabel));
		current.addStyleName("keyboard_navigation_active_element");
	}

	private void enter() {
		markCurrentItem();
		this.speakOption(currentLabel);
	}
	
	private void switchItem (boolean moveNext) {
		unMarkCurrentItem();
		
		currentLabel += moveNext ? 1 : -1;
		currentLabel = currentLabel < 0 ? 0 : currentLabel % labelsIds.size();
		speakOption(currentLabel);
		
		markCurrentItem();
	}

	private void next () {
		if (labelsIds.size() < 1) {
			return;
		}
		
		this.switchItem(true);
	}
	
	private void previous () {
		if (labelsIds.size() < 1) {
			return;
		}
		
		this.switchItem(false);
	}

	private void select() {
		if (labelsIds.size() < 1) {
			return;
		}
		
		fireClickEvent(labelsIds.get(currentLabel));
	}

	@Override
	public void enter(boolean isExiting) {
		if (labelsIds.size() < 1) {
			return;
		}

		if (isExiting) {
			this.unMarkCurrentItem();
		} else {
			this.enter();
		}
	}


	@Override
	public void space() {
		select();
	}

	@Override
	public void tab() {
		next();
	}


	@Override
	public void left() {
		previous();
	}


	@Override
	public void right() {
		next();
	}


	@Override
	public void down() {
		next();
	}


	@Override
	public void up() {
		previous();
	}


	@Override
	public void escape() {
		if (labelsIds.size() < 1) {
			return;
		}

		this.unMarkCurrentItem();
	}


	@Override
	public void customKeyCode(KeyDownEvent event) {
	}

	@Override
	public void shiftTab() {
		previous();
	}
	
	private void speak (TextToSpeechVoice t1, TextToSpeechVoice t2) {
		if (this.pageController != null) {
			List<TextToSpeechVoice> textVoices = new ArrayList<TextToSpeechVoice>();
			textVoices.add(t1);
			textVoices.add(t2);
			
			this.pageController.speak(textVoices);
		}
	}
	
	private void speakOption (int index) {
		if (index >= 0 && index < labelsIds.size()) {
			final Label label = labels.get(labelsIds.get(index));
			this.speak(
				TextToSpeechVoice.create(label.getText(), this.module.getLangAttribute()),
				TextToSpeechVoice.create()
			);
		}
	}

	@Override
	public void setWCAGStatus (boolean isWCAGOn) {
		this.isWCAGOn = isWCAGOn;
	}

	@Override
	public void setPageController (PageController pc) {
		this.setWCAGStatus(true);
		this.pageController = pc;
	}

	@Override
	public String getLang () {
		return this.module.getLangAttribute();
	}
}
