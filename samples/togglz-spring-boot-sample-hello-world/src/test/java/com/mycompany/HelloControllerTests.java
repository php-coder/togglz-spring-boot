package com.mycompany;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class HelloControllerTests {

//    @Rule
//    public TogglzRule togglzRule = TogglzRule.allEnabled(MyFeatures.class);

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(print())
                .build();
    }

    @Test
    public void testHelloWorldFeatureEnabled() throws Exception {
//        togglzRule.enable(MyFeatures.HELLO_WORLD);
//        assertTrue(MyFeatures.HELLO_WORLD.isActive());

        mockMvc.perform(get(""))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from Spring Boot!"));
    }

    @Test
    public void testHelloWorldFeatureDisabled() throws Exception {
//        togglzRule.disable(MyFeatures.HELLO_WORLD);
//        assertFalse(MyFeatures.HELLO_WORLD.isActive());

        mockMvc.perform(get(""))
                .andExpect(status().isNotFound());
    }
}
