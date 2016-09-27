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
package org.davidmendoza.esu.admin.usuarios;

import org.davidmendoza.esu.shared.Perfil;
import org.davidmendoza.esu.shared.PerfilService;
import org.davidmendoza.esu.shared.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
@RequestMapping("/admin/usuarios")
public class UsuarioController {
    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService service;
    @Autowired
    private PerfilValidator perfilValidator;
    @Autowired
    private PerfilService perfilService;

    @GetMapping
    public String lista(@RequestParam(required = false) Integer pagina, Model model) {
        pagina = (pagina != null && pagina >= 0) ? pagina : 0;
        model.addAttribute("pagina", pagina);

        Sort sort = new Sort("apellido");
        PageRequest pr = new PageRequest(pagina, 10, sort);
        model.addAttribute("usuarios", service.lista(pr));

        return "admin/usuarios/lista";
    }

    @GetMapping("/editar/{usuarioId}")
    public String editar(@PathVariable Long usuarioId, Model model) {
        model.addAttribute("perfil", service.obtienePerfil(usuarioId));

        return "admin/usuarios/editar";
    }
    
    @PostMapping(value = "/editar")
    public String editar(@ModelAttribute("perfil") Perfil perfil, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        perfilValidator.validate(perfil, bindingResult);
        if (bindingResult.hasErrors()) {
            log.warn("No se pudo actualizar usuario. {}", bindingResult.getAllErrors());
            return "admin/usuario/editar";
        }
        Usuario usuario = perfil.getUsuario();

        try {
            perfilService.actualiza(perfil);
        } catch (Exception e) {
            log.error("No se pudo actualizar usuario", e);
            bindingResult.reject("No se pudo actualizar usuario. {}", e.getMessage());
            return "admin/usuario/editar";
        }
        return "redirect:/admin/usuario/ver/" + usuario.getId();
    }

    
}
