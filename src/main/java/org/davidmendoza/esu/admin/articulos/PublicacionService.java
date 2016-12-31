/*
 * The MIT License
 *
 * Copyright 2016 Southwestern Adventist University.
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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.davidmendoza.esu.admin.articulos.ArticuloRepository;
import org.davidmendoza.esu.populares.PopularRepository;
import org.davidmendoza.esu.admin.articulos.PublicacionRepository;
import org.davidmendoza.esu.populares.Popular;
import org.davidmendoza.esu.admin.usuarios.Usuario;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Service
@Transactional
public class PublicacionService {

    private static final Logger log = LoggerFactory.getLogger(PublicacionService.class);

    @Autowired
    private PublicacionRepository publicacionRepository;
    @Autowired
    private PopularRepository popularRepository;
    @Autowired
    private ArticuloRepository articuloRepository;

    @Transactional(readOnly = true)
    public Publicacion obtiene(Integer anio, String trimestre, String leccion, String dia, String tipo) {
        if (tipo.equals("leccion")) {
            List<Publicacion> publicaciones = publicacionRepository.findByAnioAndTrimestreAndLeccionAndDiaAndTipoAndEstatus(anio, trimestre, leccion, dia, tipo, "PUBLICADO");
            if (publicaciones != null && !publicaciones.isEmpty()) {
                return publicaciones.get(0);
            }
        } else {
            List<Publicacion> publicaciones = publicacionRepository.findByAnioAndTrimestreAndLeccionAndTipoAndEstatus(anio, trimestre, leccion, tipo, "PUBLICADO");
            if (publicaciones != null && !publicaciones.isEmpty()) {
                return publicaciones.get(0);
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<Publicacion> obtiene(Integer anio, String trimestre, String leccion, String tipo) {
        return publicacionRepository.findByAnioAndTrimestreAndLeccionAndTipoAndEstatus(anio, trimestre, leccion, tipo, "PUBLICADO");
    }

    @Transactional(readOnly = true)
    public List<Publicacion> populares(Pageable pageable) {

        Page<Popular> page = popularRepository.findAll(pageable);
        List<Publicacion> publicaciones = new ArrayList<>();
        for (Popular popular : page.getContent()) {
            publicaciones.add(popular.getPublicacion());
        }

        publicaciones.stream().forEach((z) -> {
            log.debug("{} : {} : {} : {} : {} : {}", z.getAnio(), z.getTrimestre(), z.getLeccion(), z.getTipo(), z.getDia(), z.getTema());
            if (StringUtils.isNotBlank(z.getArticulo().getDescripcion())) {
                String descripcion = z.getArticulo().getDescripcion();
                if (StringUtils.isNotBlank(descripcion) && !StringUtils.contains(descripcion, "iframe")) {
                    descripcion = new HtmlToPlainText().getPlainText(Jsoup.parse(descripcion));
                    z.getArticulo().setDescripcion(StringUtils.abbreviate(descripcion, 280));
                } else if (StringUtils.isNotBlank(descripcion)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<div class='embed-responsive embed-responsive-16by9'>");
                    sb.append(descripcion);
                    sb.append("</div>");
                    z.getArticulo().setDescripcion(sb.toString());
                }
            } else {
                Articulo articulo = articuloRepository.findOne(z.getArticulo().getId());
                String contenido = articulo.getContenido();
                if (StringUtils.isNotBlank(contenido) && !StringUtils.contains(contenido, "iframe")) {
                    contenido = new HtmlToPlainText().getPlainText(Jsoup.parse(contenido));
                    z.getArticulo().setDescripcion(StringUtils.abbreviate(contenido, 280));
                } else if (StringUtils.isNotBlank(contenido)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<div class='embed-responsive embed-responsive-16by9'>");
                    sb.append(contenido);
                    sb.append("</div>");
                    z.getArticulo().setDescripcion(sb.toString());
                }
            }
        });
        return publicaciones;
    }

    public Integer agregarVista(Articulo articulo) {
        Integer vistas = articuloRepository.vistas(articulo.getId());
        if (vistas == null) {
            vistas = 0;
        }
        vistas++;
        articuloRepository.agregarVista(vistas, articulo.getId());
        return vistas;
    }

    @Transactional(readOnly = true)
    public Popular obtieneSiguientePopularEstudia(Integer posicion) {
        PageRequest pageRequest = new PageRequest(0, 1);
        List<Popular> populares = popularRepository.obtieneSiguientePopularEstudia(posicion, pageRequest);
        if (populares != null && !populares.isEmpty()) {
            return populares.get(0);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Popular obtieneSiguientePopularProfundiza(Integer posicion) {
        PageRequest pageRequest = new PageRequest(0, 1);
        List<Popular> populares = popularRepository.obtieneSiguientePopularProfundiza(posicion, pageRequest);
        if (populares != null && !populares.isEmpty()) {
            return populares.get(0);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Popular obtieneSiguientePopularComunica(Integer posicion) {
        PageRequest pageRequest = new PageRequest(0, 1);
        List<Popular> populares = popularRepository.obtieneSiguientePopularComunica(posicion, pageRequest);
        if (populares != null && !populares.isEmpty()) {
            return populares.get(0);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<Publicacion> publicaciones(Usuario autor) {
        return publicacionRepository.findByEstatusAndArticuloAutorIdOrderByDateCreated("PUBLICADO", autor.getId());
    }
    
    @Transactional(readOnly = true)
    public List<Publicacion> publicacionesUnicasDeArticulos(Usuario usuario) {
        List<String> tipos = new ArrayList<>();
        tipos.add("dialoga");
        tipos.add("comunica");
        return publicacionRepository.findByEstatusAndArticuloAutorIdAndTipoInOrderByDateCreated("PUBLICADO", usuario.getId(), tipos);
    }
}
