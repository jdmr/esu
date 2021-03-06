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
package org.davidmendoza.esu.admin.trimestres;

import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Controller
@RequestMapping("/admin/trimestres")
public class TrimestreController {

    private static final Logger log = LoggerFactory.getLogger(TrimestreController.class);

    @Autowired
    private TrimestreService service;

    @GetMapping
    public String trimestres(@RequestParam(required = false) Integer pagina, Model model) {
        pagina = (pagina != null && pagina >= 0) ? pagina : 0;
        log.debug("Agregando pagina: {}", pagina);
        model.addAttribute("pagina", pagina);

        Sort sort = new Sort(Sort.Direction.DESC, "inicia");
        PageRequest pr = new PageRequest(pagina, 10, sort);
        model.addAttribute("trimestres", service.trimestres(pr));
        return "admin/trimestres/lista";
    }

    @GetMapping("/editar/{trimestreId}")
    public String editar(@PathVariable Long trimestreId, Model model) {
        model.addAttribute("trimestre", service.obtiene(trimestreId));
        return "admin/trimestres/editar";
    }

    @PostMapping("/editar")
    public String actualizar(Model model, @Valid Trimestre trimestre, Errors errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            log.warn("No se pudo actualizar el trimestre. {}", errors.getAllErrors());
            return "admin/trimestes/editar";
        }
        try {
            service.actualiza(trimestre);
            redirectAttributes.addFlashAttribute("mensaje", "Se actualizó el trimestre " + trimestre.getNombre() + ".");
            redirectAttributes.addFlashAttribute("mensajeEstilo", "alert-success");
            return "redirect:/admin/trimestres";
        } catch (Exception e) {
            log.error("No se pudo actualizar el trimestre.", e);
        }
        return "admin/trimestes/editar";
    }
    
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Trimestre trimestre = service.nuevo();
        model.addAttribute("trimestre", trimestre);
        return "admin/trimestres/nuevo";
    }
    
    @GetMapping("/eliminar/{trimestreId}")
    public String eliminar(@PathVariable Long trimestreId, Model model, RedirectAttributes redirectAttributes) {
        Trimestre trimestre = service.obtiene(trimestreId);
        if (trimestre != null) {
            service.elimina(trimestre);
            redirectAttributes.addFlashAttribute("mensaje", "Se ha eliminado el trimestre "+trimestre.getNombre()+".");
        }
        return "redirect:/admin/trimestres";
    }
}
