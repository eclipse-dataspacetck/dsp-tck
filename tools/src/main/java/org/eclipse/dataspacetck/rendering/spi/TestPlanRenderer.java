package org.eclipse.dataspacetck.rendering.spi;

import org.eclipse.dataspacetck.document.model.Category;
import org.eclipse.dataspacetck.document.model.TestMethod;
import org.eclipse.dataspacetck.document.model.TestSuite;

public interface TestPlanRenderer {

    void title(String title);

    void subTitle(String subTitle);

    void category(Category category);

    void testSuite(TestSuite displayName);

    void testCase(TestMethod testMethod);

    String render();
}
