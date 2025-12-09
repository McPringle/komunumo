/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package app.komunumo.vaadin.components;

import app.komunumo.domain.core.image.control.ImageService;
import app.komunumo.domain.core.layout.boundary.WebsiteLayout;
import app.komunumo.test.BrowserTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageUploadBT extends BrowserTest {

    @Autowired
    private ImageService imageService;

    @Test
    void uploadAndDeleteWithButton() {
        final var testUrl = getInstanceUrl() + "test/image-upload";
        final var page = getPage();

        // navigate to home page
        page.navigate(testUrl);
        page.waitForSelector(getInstanceNameSelector());

        // upload image
        captureScreenshot("uploadAndDeleteWithButton_beforeUpload");
        final var imagePath = Path.of("src/test/resources/import/test.png");
        final var uploadInput = page.locator("vaadin-upload input[type='file']");
        uploadInput.setInputFiles(imagePath);
        page.waitForSelector(".image-upload-preview",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
        captureScreenshot("uploadAndDeleteWithButton_afterUpload");

        // delete uploaded image with delete button
        page.locator("vaadin-button.image-upload-delete").click();
        page.waitForSelector(".image-upload-preview",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        captureScreenshot("uploadAndDeleteWithButton_afterDelete");
    }

    @Test
    void uploadAndDeleteWithX() {
        final var testUrl = getInstanceUrl() + "test/image-upload";
        final var page = getPage();

        // navigate to home page
        page.navigate(testUrl);
        page.waitForSelector(getInstanceNameSelector());

        // upload image
        captureScreenshot("uploadAndDeleteWithX_beforeUpload");
        final var imagePath = Path.of("src/test/resources/import/test.png");
        final var uploadInput = page.locator("vaadin-upload input[type='file']");
        uploadInput.setInputFiles(imagePath);
        page.waitForSelector(".image-upload-preview",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
        captureScreenshot("uploadAndDeleteWithX_afterUpload");

        // delete uploaded image with X button
        final var removeButton = page.locator("vaadin-upload-file >> button[part='remove-button']");
        removeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        removeButton.click();
        page.waitForSelector(".image-upload-preview",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        captureScreenshot("uploadAndDeleteWithX_afterDelete");
    }

    @Test
    void setExistingImageAndDelete() {
        final var imageId = imageService.getAllImageIds().getFirst();
        final var testUrl = getInstanceUrl() + "test/image-upload?imageId=" + imageId;
        final var page = getPage();

        // navigate to home page
        page.navigate(testUrl);
        page.waitForSelector(getInstanceNameSelector());

        // set image
        page.waitForSelector(".image-upload-preview",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
        captureScreenshot("setExistingImageAndDelete_afterNavigation");

        // delete existing image with Delete button
        page.locator("vaadin-button.image-upload-delete").click();
        page.waitForSelector(".image-upload-preview",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        captureScreenshot("setExistingImageAndDelete_afterDelete");

        // image should still exist in storage
        assertThat(imageService.getImage(imageId)).isNotEmpty();
    }

    @AnonymousAllowed
    @Route(value = "test/image-upload", layout = WebsiteLayout.class)
    public static class ImageUploadView extends VerticalLayout implements AfterNavigationObserver {

        private final @NotNull ImageService imageService;

        public ImageUploadView(final @NotNull ImageService imageService) {
            this.imageService = imageService;
            add(new H2("Image Upload Test"));
        }

        @Override
        public void afterNavigation(final @NotNull AfterNavigationEvent afterNavigationEvent) {
            final var imageUpload = new ImageUpload(imageService);
            add(imageUpload);

            afterNavigationEvent
                    .getLocation()
                    .getQueryParameters()
                    .getSingleParameter("imageId").ifPresent(imageIdString -> {
                        final var imageId = UUID.fromString(imageIdString);
                        final var imageDto = imageService.getImage(imageId)
                                .orElseThrow();
                        imageUpload.setValue(imageDto);
                    });
        }
    }
}
