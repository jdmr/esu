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
package org.davidmendoza.esu.admin.articulos;

import java.util.List;
import org.apache.commons.lang.StringUtils;
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
public class ArticuloService {

    @Autowired
    private ArticuloRepository repository;
    @Autowired
    private PublicacionRepository publicacionRepository;

    @Transactional(readOnly = true)
    Page<Articulo> lista(PageRequest pr) {
        Page<Articulo> articulos = repository.findAll(pr);
        for (Articulo articulo : articulos) {
            articulo.setDescripcion(StringUtils.abbreviate(articulo.getDescripcion(), 100));
            PageRequest pr2 = new PageRequest(0, 1);
            List<Publicacion> publicaciones = publicacionRepository.findByEstatusAndArticuloId("PUBLICADO", articulo.getId(), pr2);
            if (publicaciones != null && !publicaciones.isEmpty()) {
                articulo.setPublicacion(publicaciones.get(0));
            } else {
                articulo.setPublicacion(new Publicacion());
            }
        }
        return articulos;
    }

}
