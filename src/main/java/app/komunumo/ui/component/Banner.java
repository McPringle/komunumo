package app.komunumo.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;

public class Banner extends Div {

    public Banner() {
        super();
        addClassName("banner");
        add(new Paragraph("Demo Modus!"));
    }
}
