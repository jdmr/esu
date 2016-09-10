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
package org.davidmendoza.esu.inicio;

import java.util.TimeZone;
import org.davidmendoza.esu.shared.Inicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Controller
public class InicioController {

    private static final Logger log = LoggerFactory.getLogger(InicioController.class);
    
    @Autowired
    private InicioService service;
    
    @GetMapping({"/","/inicio"})
    public String index(TimeZone timeZone) {
        Inicio inicio = service.inicio(timeZone);
        if (inicio != null) {
            StringBuilder url = new StringBuilder();
            url.append("redirect:/inicio/");
            url.append(inicio.getAnio()).append("/");
            url.append(inicio.getTrimestre()).append("/");
            url.append(inicio.getLeccion()).append("/");
            url.append(inicio.getDia());
            return url.toString();
        } else {
            return "inicio/trimestre";
        }
    }
    
    @GetMapping("/inicio/{anio}/{trimestre}/{leccion}/{dia}")
    public String inicio(Model model, @ModelAttribute Inicio inicio) {

        log.info("Anio: {} | Trimestre: {} | Leccion: {} | Dia: {}", new Object[]{inicio.getAnio(), inicio.getTrimestre(), inicio.getLeccion(), inicio.getDia()});

        inicio = service.inicio(inicio);

        model.addAttribute("inicio", inicio);

        return "inicio/inicio";
    }
    
}
