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
package org.davidmendoza.esu.admin.trimestres;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.davidmendoza.esu.admin.trimestres.TrimestreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Service
@Transactional
public class TrimestreService {

    private static final Logger log = LoggerFactory.getLogger(TrimestreService.class);

    @Autowired
    private TrimestreRepository trimestreRepository;

    @Transactional(readOnly = true)
    public Trimestre obtiene(Date fecha) {
        return trimestreRepository.obtiene(fecha);
    }

    @Transactional(readOnly = true)
    public Trimestre obtiene(String nombre) {
        log.debug("Buscando trimestre: " + nombre);
        return trimestreRepository.buscaPorNombre(nombre);
    }

    @Transactional(readOnly = true)
    public Page<Trimestre> trimestres(PageRequest pr) {
        return trimestreRepository.findAll(pr);
    }

    @Transactional(readOnly = true)
    public Trimestre obtiene(Long trimestreId) {
        return trimestreRepository.findOne(trimestreId);
    }

    public Trimestre crea(Trimestre trimestre) {
        return trimestreRepository.save(trimestre);
    }

    public Trimestre actualiza(Trimestre trimestre) {
        return trimestreRepository.save(trimestre);
    }

    public Trimestre nuevo() {
        Trimestre trimestre = new Trimestre();
        Sort sort = new Sort(Sort.Direction.DESC, "inicia");
        PageRequest pr = new PageRequest(0, 1, sort);
        Page<Trimestre> ultimos = trimestreRepository.findAll(pr);
        Trimestre ultimo = ultimos.iterator().next();
        trimestre.setInicia(ultimo.getTermina());

        LocalDate date = trimestre.getInicia().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String t = "" + date.getYear();
        switch (date.getMonthValue()) {
            case 12:
                t = (date.getYear() + 1) + "t1";
                break;
            case 3:
                t += "t2";
                break;
            case 6:
                t += "t3";
                break;
            case 9:
                t += "t4";
        }
        trimestre.setNombre(t);

        LocalDate termina = date.plusWeeks(13);
        trimestre.setTermina(Date.from(termina.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        trimestre.setPublicado(Boolean.TRUE);

        return trimestre;
    }

    public void elimina(Trimestre trimestre) {
        trimestreRepository.delete(trimestre);
    }

}
