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
package app.komunumo.domain.core.image.control;

import org.junit.jupiter.api.Test;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

import static org.assertj.core.api.Assertions.assertThat;

class SvgHelperHardenedTransformerTest {

    @Test
    void newHardenedTransformer_setsUriResolver_and_itReturnsNull() throws Exception {
        Transformer t = SvgHelper.newHardenedTransformer(TransformerFactory.newInstance());

        assertThat(t.getURIResolver()).as("URIResolver must be set").isNotNull();
        assertThat(t.getURIResolver().resolve("any-href", "any-base"))
                .as("URIResolver should reject external resolution")
                .isNull();
    }

    @Test
    void newHardenedTransformer_handlesUnsupportedAttributes_andStillWorks() throws Exception {
        Transformer t = SvgHelper.newHardenedTransformer(new ThrowingAttributeTransformerFactory());

        // We reached here => catch branch executed without failing
        assertThat(t).isNotNull();
        assertThat(t.getURIResolver()).isNotNull();
        assertThat(t.getURIResolver().resolve("x", "y")).isNull();
    }

    private static class ThrowingAttributeTransformerFactory extends TransformerFactory {

        private final TransformerFactory delegate = TransformerFactory.newInstance();

        @Override
        public Transformer newTransformer(final Source source) throws TransformerConfigurationException {
            return delegate.newTransformer(source);
        }

        @Override
        public Transformer newTransformer() throws TransformerConfigurationException {
            return delegate.newTransformer();
        }

        @Override
        public Templates newTemplates(final Source source) throws TransformerConfigurationException {
            return delegate.newTemplates(source);
        }

        @Override
        public Source getAssociatedStylesheet(final Source source, final String media,
                                              final String title, final String charset)
                throws TransformerConfigurationException {
            return delegate.getAssociatedStylesheet(source, media, title, charset);
        }

        @Override
        public void setURIResolver(final URIResolver resolver) {
            delegate.setURIResolver(resolver);
        }

        @Override
        public URIResolver getURIResolver() {
            return delegate.getURIResolver();
        }

        @Override
        public void setFeature(final String name, final boolean value) throws TransformerConfigurationException {
            delegate.setFeature(name, value);
        }

        @Override
        public boolean getFeature(final String name) {
            return delegate.getFeature(name);
        }

        @Override
        public void setAttribute(final String name, final Object value) {
            // Force the branch you want to cover:
            throw new IllegalArgumentException("Attributes not supported in this fake TF");
        }

        @Override
        public Object getAttribute(final String name) {
            return delegate.getAttribute(name);
        }

        @Override
        public void setErrorListener(final ErrorListener listener) {
            delegate.setErrorListener(listener);
        }

        @Override
        public ErrorListener getErrorListener() {
            return delegate.getErrorListener();
        }
    }

}
