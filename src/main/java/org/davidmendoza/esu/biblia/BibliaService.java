/*
 * The MIT License
 *
 * Copyright 2015 J. David Mendoza.
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
package org.davidmendoza.esu.biblia;

import java.util.List;
import org.davidmendoza.esu.dao.BibliaRepository;
import org.davidmendoza.esu.shared.Rv2000;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Service
@Transactional
public class BibliaService {
    
    private static final Logger log = LoggerFactory.getLogger(BibliaService.class);

    @Autowired
    private BibliaRepository bibliaRepository;

    public List<Rv2000> biblia(Integer libro, Integer capitulo, Integer versiculo, Integer versiculos) {
        log.info("Biblia: {} : {} : {} : {}", libro, capitulo, versiculo, versiculos);
        Long versiculoId = bibliaRepository.getVersiculoId(libro, capitulo, versiculo);
        log.info("Biblia: {} : {}", versiculoId, versiculoId + versiculos);
        return bibliaRepository.getVersiculos(versiculoId, versiculoId + versiculos);
    }

    public List<Rv2000> biblia(Long versiculoId, Integer versiculos) {
        log.info("Biblia: {} : {}", versiculoId, versiculos);
        return bibliaRepository.getVersiculos(versiculoId, versiculoId + versiculos);
    }
}
