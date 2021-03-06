/*
 * The MIT License
 *
 * Copyright 2016 J. David Mendoza.
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
package org.davidmendoza.esu.populares;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
public interface PopularRepository extends JpaRepository<Popular, Integer> {

    @Query("select p from Popular p where p.id > :posicion and p.publicacion.tipo = 'dialoga' order by id")
    public List<Popular> obtieneSiguientePopularProfundiza(@Param("posicion") Integer posicion, Pageable pageable);

    @Query("select p from Popular p where p.id > :posicion and p.publicacion.tipo = 'comunica' order by id")
    public List<Popular> obtieneSiguientePopularComunica(@Param("posicion") Integer posicion, Pageable pageable);

    @Query("select p from Popular p where p.id > :posicion and p.publicacion.tipo = 'leccion' order by id")
    public List<Popular> obtieneSiguientePopularEstudia(@Param("posicion") Integer posicion, Pageable pageable);

}
