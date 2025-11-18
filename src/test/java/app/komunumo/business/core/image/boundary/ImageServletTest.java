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
package app.komunumo.business.core.image.boundary;

import app.komunumo.configuration.AppConfig;
import app.komunumo.configuration.DemoConfig;
import app.komunumo.configuration.FilesConfig;
import app.komunumo.configuration.InstanceConfig;
import app.komunumo.configuration.MailConfig;
import app.komunumo.data.dto.ContentType;
import app.komunumo.business.core.image.entity.ImageDto;
import app.komunumo.business.core.image.control.ImageService;
import app.komunumo.util.ImageUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
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

    private AppConfig getAppConfigMock() {
        final var userHome = System.getProperty("user.home");
        final var basedir = Path.of(userHome, ".komunumo", "test");

        final var demoConfig = new DemoConfig(false, "");
        final var filesConfig = new FilesConfig(basedir);
        final var mailConfig = new MailConfig("noreply@foo.bar", "support@foo.bar");
        final var instanceConfig = new InstanceConfig("admin@foo.bar");

        return new AppConfig("0.0.0", demoConfig, filesConfig, instanceConfig, mailConfig);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "/images/test.jpg",
            "/placeholder.svg",
            "/placeholder-000x000.svg",
            "/placeholder-100x000.svg",
            "/placeholder-000x100.svg"
    })
    void redirectsTo404Page_whenRequestIsInvalid(final @Nullable String pathInfo) throws IOException {
        // Arrange
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var appConfig = getAppConfigMock();
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn(pathInfo);

        final var servlet = new ImageServlet(appConfig, imageService);

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
        final var appConfig = getAppConfigMock();
        final var imageService = mock(ImageService.class);

        final var pathInfo = "/images/afc3478d-2c92-41b5-b89f-2a9111d79c73.jpg";
        final var imageId = UUID.fromString("afc3478d-2c92-41b5-b89f-2a9111d79c73");

        when(request.getPathInfo()).thenReturn(pathInfo);
        when(imageService.getImage(imageId)).thenReturn(Optional.empty());

        final var servlet = new ImageServlet(appConfig, imageService);

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
        final var appConfig = getAppConfigMock();
        final var imageService = mock(ImageService.class);

        final var pathInfo = "/images/11111111-1111-1111-1111-111111111111.jpg";
        final var imageId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final var image = new ImageDto(imageId, ContentType.IMAGE_JPEG);

        when(request.getPathInfo()).thenReturn(pathInfo);
        when(imageService.getImage(imageId)).thenReturn(Optional.of(image));

        final var servlet = new ImageServlet(appConfig, imageService);

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
        final var image = new ImageDto(imageId, ContentType.IMAGE_JPEG);

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var appConfig = getAppConfigMock();
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


        final var servlet = new ImageServlet(appConfig, imageService);

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
        final var image = new ImageDto(imageId, ContentType.IMAGE_JPEG);

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var outputStream = mock(ServletOutputStream.class);
        final var appConfig = getAppConfigMock();
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/images/" + imageId + ".jpg");
        when(response.getOutputStream()).thenReturn(outputStream);
        when(imageService.getImage(imageId)).thenReturn(Optional.of(image));

        // working stream that successfully calls `.transferTo(...)`
        final var inputStream = spy(new ByteArrayInputStream("demo".getBytes()));

        final var servlet = new ImageServlet(appConfig, imageService);

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
        final var appConfig = getAppConfigMock();
        final var imageService = mock(ImageService.class);
        final var servlet = new ImageServlet(appConfig, imageService);

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
        final var appConfig = getAppConfigMock();
        final var imageService = mock(ImageService.class);

        final var pathInfo = "/images/11111111-1111-1111-1111-111111111111.jpg";
        final var imageId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final var image = new ImageDto(imageId, ContentType.IMAGE_JPEG);

        when(request.getPathInfo()).thenReturn(pathInfo);
        when(imageService.getImage(imageId)).thenReturn(Optional.of(image));

        doThrow(new IOException("Redirect failed"))
                .when(response).sendRedirect("/error/500");

        final var servlet = new ImageServlet(appConfig, imageService);

        // Mock static method: ImageUtil.loadImage(image) → Optional.empty()
        try (MockedStatic<ImageUtil> mockedStatic = mockStatic(ImageUtil.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> ImageUtil.loadImage(any())).thenReturn(Optional.empty());

            servlet.doGet(request, response);

            verify(imageService).getImage(imageId);
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    void redirectsTo500Page_whenPrintWriterThrowsIOException() throws IOException {
        // Arrange
        final var appConfig = getAppConfigMock();
        final var imageService = mock(ImageService.class);
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var printWriter = mock(PrintWriter.class);

        when(request.getPathInfo()).thenReturn("/placeholder-100x200.svg");
        when(response.getWriter()).thenReturn(printWriter);
        doThrow(new IOException("Writing failed"))
                .when(printWriter).write(anyString());

        // Act
        final var servlet = new ImageServlet(appConfig, imageService);
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
        final var stringWriter = new StringWriter();
        final var appConfig = getAppConfigMock();
        final var imageService = mock(ImageService.class);

        when(request.getPathInfo()).thenReturn("/placeholder-100x200.svg");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Act
        final var servlet = new ImageServlet(appConfig, imageService);
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("image/svg+xml");
        verify(response).setHeader("Cache-Control", "public, max-age=86400");
        verify(response, never()).sendRedirect("/error/404");
        verify(response, never()).sendRedirect("/error/500");
        verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response, never()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        final var output = stringWriter.toString();
        assertThat(output.trim())
                .startsWith("<?xml")
                .contains("<svg xmlns=\"http://www.w3.org/2000/svg\"")
                .contains("width=\"100\"")
                .contains("height=\"200\"")
                .contains("<g ")
                .contains("id=\"Logo\"")
                .endsWith("</svg>");
    }

}
