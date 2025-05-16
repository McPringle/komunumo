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
package app.komunumo.ui.servlets;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.ImageService;
import app.komunumo.util.ImageUtil;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ImageServletTest {

    @Test
    void returnsNotFound_whenImageIdIsInvalid() {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/images/test.jpg");

        final var servlet = new ImageServlet(imageService);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verifyNoInteractions(imageService);
    }

    @Test
    void returnsNotFound_whenImageIsNotInDatabase() {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        final var pathInfo = "/images/afc3478d-2c92-41b5-b89f-2a9111d79c73.jpg";
        final var imageId = UUID.fromString("afc3478d-2c92-41b5-b89f-2a9111d79c73");

        when(request.getPathInfo()).thenReturn(pathInfo);
        when(imageService.getImage(imageId)).thenReturn(Optional.empty());

        final var servlet = new ImageServlet(imageService);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(imageService).getImage(imageId);
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void returnsNotFound_whenImageStreamIsMissing() {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        final var pathInfo = "/images/11111111-1111-1111-1111-111111111111.jpg";
        final var imageId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final var image = new ImageDto(imageId, ContentType.IMAGE_JPEG, "test.jpg");

        when(request.getPathInfo()).thenReturn(pathInfo);
        when(imageService.getImage(imageId)).thenReturn(Optional.of(image));

        final var servlet = new ImageServlet(imageService);

        // Mock static method: ImageUtil.loadImage(image) → Optional.empty()
        try (MockedStatic<ImageUtil> mockedStatic = mockStatic(ImageUtil.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> ImageUtil.loadImage(any())).thenReturn(Optional.empty());

            servlet.doGet(request, response);

            verify(imageService).getImage(imageId);
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Test
    void returnsInternalServerError_whenStreamingFails() {
        // Arrange
        final UUID imageId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final var image = new ImageDto(imageId, ContentType.IMAGE_JPEG, "test.jpg");

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/images/" + imageId + ".jpg");
        when(imageService.getImage(imageId)).thenReturn(Optional.of(image));

        @SuppressWarnings("unchecked")
        final Optional<InputStream> brokenOptional = mock(Optional.class);
        when(brokenOptional.isEmpty()).thenReturn(false);
        when(brokenOptional.orElseThrow(any()))
                .thenAnswer(invocation -> {
                    var supplier = invocation.getArgument(0, java.util.function.Supplier.class);
                    throw (IOException) supplier.get();
                });


        final var servlet = new ImageServlet(imageService);

        try (var mockedStatic = mockStatic(ImageUtil.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> ImageUtil.loadImage(image)).thenReturn(brokenOptional);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void streamsImageSuccessfully_whenAllConditionsAreMet() throws IOException {
        // Arrange
        final UUID imageId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final var image = new ImageDto(imageId, ContentType.IMAGE_JPEG, "test.jpg");

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var outputStream = mock(ServletOutputStream.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/images/" + imageId + ".jpg");
        when(response.getOutputStream()).thenReturn(outputStream);
        when(imageService.getImage(imageId)).thenReturn(Optional.of(image));

        // working stream that successfully calls `.transferTo(...)`
        final var inputStream = spy(new ByteArrayInputStream("demo".getBytes()));

        final var servlet = new ImageServlet(imageService);

        try (var mockedStatic = mockStatic(ImageUtil.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> ImageUtil.loadImage(image)).thenReturn(Optional.of(inputStream));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setContentType("image/jpeg");
            verify(response).setHeader("Cache-Control", "public, max-age=86400");
            verify(inputStream).transferTo(outputStream);
            verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
            verify(response, never()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
