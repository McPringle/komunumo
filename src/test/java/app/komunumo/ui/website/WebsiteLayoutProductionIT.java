package app.komunumo.ui.website;

import static app.komunumo.util.TestUtil.assertContainsExactlyOneInstanceOf;

import app.komunumo.ui.IntegrationTest;
import app.komunumo.ui.component.NavigationBar;
import app.komunumo.ui.component.PageFooter;
import app.komunumo.ui.component.PageHeader;
import app.komunumo.ui.website.home.HomeView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Main;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "komunumo.demo.enabled=false")
public class WebsiteLayoutProductionIT extends IntegrationTest {

    private @NotNull WebsiteLayout websiteLayout;

    @BeforeEach
    void setUp() {
        UI.getCurrent().navigate(HomeView.class);
        final var uiParent = UI.getCurrent()
                .getCurrentView()
                .getParent().orElseThrow()
                .getParent().orElseThrow();
        websiteLayout = (WebsiteLayout) uiParent;
    }

    @Test
    void testLayoutHasNoBanner()  {
        final var components = websiteLayout.getChildren().toList();
        // FIXME: Write a better assertion
        assertContainsExactlyOneInstanceOf(components,
                 PageHeader.class, NavigationBar.class, Main.class, PageFooter.class);
    }



}
