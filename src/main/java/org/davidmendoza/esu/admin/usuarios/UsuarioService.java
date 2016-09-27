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

import org.davidmendoza.esu.dao.PerfilRepository;
import org.davidmendoza.esu.dao.UsuarioRepository;
import org.davidmendoza.esu.shared.Perfil;
import org.davidmendoza.esu.shared.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Service
@Transactional
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
    
    @Autowired
    UsuarioRepository repository;
    @Autowired
    PerfilRepository perfilRepository;

    Page<Usuario> lista(PageRequest pr) {
        Page<Usuario> usuarios = repository.findAll(pr);
        for (Usuario usuario : usuarios) {
            Perfil perfil = perfilRepository.findByUsuario(usuario);
            if (perfil != null) {
                usuario.setPerfil(perfil);
            } else {
                usuario.setPerfil(new Perfil());
            }
        }
        return usuarios;
    }

    Usuario obtiene(Long usuarioId) {
        return repository.findOne(usuarioId);
    }

    Perfil obtienePerfil(Long usuarioId) {
        Usuario usuario = repository.getOne(usuarioId);
        log.debug("Obteniendo perfil: {}", usuarioId);
        Perfil perfil = perfilRepository.findByUsuario(usuario);
        if (perfil == null) {
            perfil = new Perfil();
        }
        return perfil;
    }

}
