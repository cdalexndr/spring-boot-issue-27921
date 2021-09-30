package example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WorkaroundWebDriverTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//default text execution listeners don't work for testng
//@TestExecutionListeners({
//        //MockitoTestExecutionListener.class, ResetMocksTestExecutionListener.class, //uncomment for mockito to work
//        //WorkaroundWebDriverTestExecutionListener.class //uncomment for webdriver to work
//})
public class WebDriverTest extends AbstractTestNGSpringContextTests {

    @Autowired
    WebDriver webDriver;
    @SpyBean
    SomeService someService;
    @Autowired
    ConfigurableApplicationContext ctx;

    @Test(dependsOnMethods = "changeSpyService")
    public void testResetSpyService() {
        assertEquals(someService.getValue(), 13);
    }

    @Test
    public void changeSpyService() {
        doReturn(15).when(someService).getValue();
        assertEquals(someService.getValue(), 15);
    }

    @TestConfiguration
    public static class WebDriverConfig {
        @Bean
        public WebDriver webDriver() {
            WebDriverManager.firefoxdriver().setup();
            return new FirefoxDriver(
                    new FirefoxOptions().setHeadless(true));
        }
    }

    int oldHashcode;

    @Test
    public void testWebDriver() throws Exception {
        oldHashcode = webDriver.hashCode();
    }

    @Test(dependsOnMethods = "testWebDriver")
    public void testWebDriver2() throws Exception {
        int newHashCode = webDriver.hashCode();
        //WebDriverTestExecutionListener resets webdriver instances after each method
        assertNotEquals(newHashCode, oldHashcode, "Expected diff webdriver instance");
        //if test fails, there will be hanging geckodriver and firefox processes after tests
    }

    @Test //works by default
    public void testWebDriverTestExecutionListener() {
        //added by WebDriverContextCustomizerFactory.Customizer
        assertNotNull(ctx.getBeanFactory().getRegisteredScope("webDriver"));
    }

}
