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

import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.ImageService;
import app.komunumo.util.ImageUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ImageServletTest {

    @Test
    void redirectsTo404Page_whenUrlIsNull() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn(null);

        final var servlet = new ImageServlet(imageService);

        // Act
        servlet.doGet(request, response);

        // Assert
        verifyNoInteractions(imageService);
        verify(response).sendRedirect("/error/404");
    }

    @Test
    void redirectsTo404Page_whenImageIdIsInvalid() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/images/test.jpg");

        final var servlet = new ImageServlet(imageService);

        // Act
        servlet.doGet(request, response);

        // Assert
        verifyNoInteractions(imageService);
        verify(response).sendRedirect("/error/404");
    }

    @Test
    void redirectsTo404Page_whenImageIsNotInDatabase() throws IOException {
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
        verify(response).sendRedirect("/error/404");
    }

    @Test
    void redirectsTo500Page_whenImageStreamIsMissing() throws IOException {
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
            verify(response).sendRedirect("/error/500");
        }
    }

    @Test
    void redirectsTo500Page_whenStreamingFails() throws IOException {
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
            verify(response).sendRedirect("/error/500");
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
            verify(response, never()).sendRedirect("/error/404");
            verify(response, never()).sendRedirect("/error/500");
            verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
            verify(response, never()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void setsNotFoundStatus_whenRedirectTo404PageFails() throws IOException {
        // Arrange
        final var imageService = mock(ImageService.class);
        final var servlet = new ImageServlet(imageService);

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        final var pathInfo = "/images/invalid.jpg";
        when(request.getPathInfo()).thenReturn(pathInfo);

        doThrow(new IOException("Redirect failed"))
                .when(response).sendRedirect("/error/404");

        try (MockedStatic<ImageUtil> mockedStatic = mockStatic(ImageUtil.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> ImageUtil.extractImageIdFromUrl(pathInfo)).thenReturn(null);

            // Act
            servlet.doGet(request, response);
        }

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void setsInternalServerErrorStatus_whenRedirectTo500PageFails() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        final var pathInfo = "/images/11111111-1111-1111-1111-111111111111.jpg";
        final var imageId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final var image = new ImageDto(imageId, ContentType.IMAGE_JPEG, "test.jpg");

        when(request.getPathInfo()).thenReturn(pathInfo);
        when(imageService.getImage(imageId)).thenReturn(Optional.of(image));

        doThrow(new IOException("Redirect failed"))
                .when(response).sendRedirect("/error/500");

        final var servlet = new ImageServlet(imageService);

        // Mock static method: ImageUtil.loadImage(image) → Optional.empty()
        try (MockedStatic<ImageUtil> mockedStatic = mockStatic(ImageUtil.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> ImageUtil.loadImage(any())).thenReturn(Optional.empty());

            servlet.doGet(request, response);

            verify(imageService).getImage(imageId);
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void redirectsTo404Page_whenPlaceholderImageDimensionsAreZero() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/placeholder-000x000.svg");

        final var servlet = new ImageServlet(imageService);

        // Act
        servlet.doGet(request, response);

        // Assert
        verifyNoInteractions(imageService);
        verify(response).sendRedirect("/error/404");
    }

    @Test
    void redirectsTo404Page_whenPlaceholderImageWidthIsZero() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/placeholder-100x000.svg");

        final var servlet = new ImageServlet(imageService);

        // Act
        servlet.doGet(request, response);

        // Assert
        verifyNoInteractions(imageService);
        verify(response).sendRedirect("/error/404");
    }

    @Test
    void redirectsTo404Page_whenPlaceholderImageHeightIsZero() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/placeholder-000x100.svg");

        final var servlet = new ImageServlet(imageService);

        // Act
        servlet.doGet(request, response);

        // Assert
        verifyNoInteractions(imageService);
        verify(response).sendRedirect("/error/404");
    }

    @Test
    void redirectsTo404Page_whenPlaceholderImageDimensionNotFound() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/placeholder.svg");

        // Act
        final var servlet = new ImageServlet(imageService);
        servlet.doGet(request, response);

        // Assert
        verifyNoInteractions(imageService);
        verify(response).sendRedirect("/error/404");
    }

    @Test
    void redirectsTo500Page_whenPrintWriterThrowsIOException() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);
        final var printWriter = mock(PrintWriter.class);

        when(request.getPathInfo()).thenReturn("/placeholder-100x200.svg");
        when(response.getWriter()).thenReturn(printWriter);
        doThrow(new IOException("Writing failed"))
                .when(printWriter).write(anyString());

        // Act
        final var servlet = new ImageServlet(imageService);
        servlet.doGet(request, response);

        // Assert
        verifyNoInteractions(imageService);
        verify(response).sendRedirect("/error/500");
    }

    @Test
    void streamsPlaceholderSuccessfully() throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var imageService = mock(ImageService.class);
        final var stringWriter = new StringWriter();

        when(request.getPathInfo()).thenReturn("/placeholder-100x200.svg");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Act
        final var servlet = new ImageServlet(imageService);
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("image/svg+xml");
        verify(response).setHeader("Cache-Control", "public, max-age=86400");
        verify(response, never()).sendRedirect("/error/404");
        verify(response, never()).sendRedirect("/error/500");
        verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response, never()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        final var output = stringWriter.toString();
        assertThat(output)
                .startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\"")
                .contains("width=\"100\" height=\"200\"")
                .endsWith("</svg>");
    }

}
