/*
 * The MIT License
 *
 * Copyright 2016 Universidad de Montemorelos.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.davidmendoza.esu.conocenos;

import static org.hamcrest.Matchers.containsString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ConocenosControllerTest {
    
    private static final Logger log = LoggerFactory.getLogger(ConocenosControllerTest.class);

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;

    @Before
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void shouldShowConocenos() throws Exception {
        mvc.perform(get("/conocenos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Conócenos: Acerca de ESU - ESU</title>")))
                .andDo(print());
        
        mvc.perform(get("/conocenos/nosotros"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Conócenos: Acerca de ESU - ESU</title>")))
                .andDo(print());
    }
    
    @Test
    public void shouldShowContacto() throws Exception {
        mvc.perform(get("/conocenos/contacto"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Conócenos: Contáctanos - ESU</title>")))
                .andDo(print());
    }
    
    @Test
    public void shouldShowEquipo() throws Exception {
        mvc.perform(get("/conocenos/equipo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Conócenos: Equipo - ESU</title>")))
                .andDo(print());
    }
    
}
