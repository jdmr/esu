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
package org.davidmendoza.esu.inicio;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;
import org.davidmendoza.esu.shared.Inicio;
import org.davidmendoza.esu.shared.Publicacion;
import org.davidmendoza.esu.shared.PublicacionService;
import org.davidmendoza.esu.shared.Trimestre;
import org.davidmendoza.esu.shared.TrimestreService;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class InicioService {

    private static final Logger log = LoggerFactory.getLogger(InicioService.class);

    private final NumberFormat dosDigitos = NumberFormat.getInstance();
    private final NumberFormat nf = NumberFormat.getInstance();

    {
        dosDigitos.setMinimumIntegerDigits(2);
    }

    @Autowired
    private TrimestreService trimestreService;
    @Autowired
    private PublicacionService publicacionService;

    @Transactional(readOnly = true)
    public Inicio inicio(TimeZone timeZone) {
        log.debug("Obteniendo dia");
        Calendar calendar = new GregorianCalendar(timeZone);
        return inicio(calendar);
    }

    @Transactional(readOnly = true)
    public Inicio inicio(Calendar hoy) {
        log.debug("Hoy: {}", hoy.getTime());

        if (hoy.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                && hoy.get(Calendar.HOUR_OF_DAY) < 12) {
            hoy.add(Calendar.DAY_OF_WEEK, -1);
        }

        Trimestre trimestre = trimestreService.obtiene(hoy.getTime());

        if (trimestre != null) {
            DateTime a = new DateTime(trimestre.getInicia());
            DateTime b = new DateTime(hoy);
            Weeks c = Weeks.weeksBetween(a, b);
            int weeks = c.getWeeks() + 1;
            log.debug("Weeks: {}", weeks);
            StringBuilder leccion = new StringBuilder();
            leccion.append("l").append(dosDigitos.format(weeks));

            Inicio inicio = new Inicio();
            inicio.setAnio(trimestre.getNombre().substring(0, 4));
            inicio.setTrimestre(trimestre.getNombre().substring(4));
            inicio.setLeccion(leccion.toString());
            inicio.setDia(obtieneDia(hoy.get(Calendar.DAY_OF_WEEK)));
            return inicio;
        } else {
            return null;
        }

    }

    @Transactional(readOnly = true)
    public Inicio inicio(Inicio inicio) {
        Integer anio = new Integer(inicio.getAnio());
        String trimestre = inicio.getTrimestre();
        String leccion = inicio.getLeccion();
        String dia = inicio.getDia();
        if (StringUtils.isBlank(dia)) {
            dia = obtieneDia(new GregorianCalendar(Locale.ENGLISH).get(Calendar.DAY_OF_WEEK));
            inicio.setDia(dia);
        }
        log.debug("DIA: {}", dia);

        Publicacion leccion1 = publicacionService.obtiene(anio, trimestre, leccion, dia, "leccion");

        Publicacion versiculo = publicacionService.obtiene(anio, trimestre, leccion, null, "versiculo");

        Publicacion video = publicacionService.obtiene(anio, trimestre, leccion, null, "video");

        Publicacion podcast = publicacionService.obtiene(anio, trimestre, leccion, null, "podcast");

        List<Publicacion> dialoga = publicacionService.obtiene(anio, trimestre, leccion, "dialoga");

        List<Publicacion> comunica = publicacionService.obtiene(anio, trimestre, leccion, "comunica");

        PageRequest page = new PageRequest(0, 10, Sort.Direction.ASC, "id");

        List<Publicacion> populares = publicacionService.populares(page);

        Trimestre t = trimestreService.obtiene(anio + trimestre);
        if (t != null) {
            try {
                Calendar cal = new GregorianCalendar(Locale.ENGLISH);
                cal.setTime(t.getInicia());
                cal.add(Calendar.SECOND, 1);
                cal.set(Calendar.DAY_OF_WEEK, obtieneDia(dia));
                int weeks = ((Long) nf.parse(leccion.substring(1))).intValue();
                if (dia.equals("sabado")) {
                    weeks--;
                }
                cal.add(Calendar.WEEK_OF_YEAR, weeks);
                Date hoy = cal.getTime();

                if (leccion1 != null) {
                    inicio.setPublicacion(leccion1);
                    inicio.setTitulo(leccion1.getArticulo().getTitulo());
                }
                inicio.setDialoga(dialoga);
                inicio.setComunica(comunica);
                inicio.setVideo(video);
                inicio.setPodcast(podcast);
                inicio.setVersiculo(versiculo);
                inicio.setHoy(hoy);
                int max = Math.max(dialoga.size(), comunica.size());
                List<Publicacion> articulos = new ArrayList<>();
                for (int i = 0; i < max; i++) {
                    if (i < dialoga.size()) {
                        articulos.add(dialoga.get(i));
                    }
                    if (i < comunica.size()) {
                        articulos.add(comunica.get(i));
                    }
                }
                articulos.addAll(populares);
                inicio.setArticulos(articulos);

                return inicio;
            } catch (ParseException e) {
                log.error("No pude poner la fecha de hoy", e);
            }
        }

        return null;
    }

    private String obtieneDia(int dia) {
        switch (dia) {
            case Calendar.SUNDAY:
                return "domingo";
            case Calendar.MONDAY:
                return "lunes";
            case Calendar.TUESDAY:
                return "martes";
            case Calendar.WEDNESDAY:
                return "miercoles";
            case Calendar.THURSDAY:
                return "jueves";
            case Calendar.FRIDAY:
                return "viernes";
            default:
                return "sabado";
        }
    }

    private Integer obtieneDia(String dia) {
        switch (dia) {
            case "domingo":
                return Calendar.SUNDAY;
            case "lunes":
                return Calendar.MONDAY;
            case "martes":
                return Calendar.TUESDAY;
            case "miercoles":
                return Calendar.WEDNESDAY;
            case "jueves":
                return Calendar.THURSDAY;
            case "viernes":
                return Calendar.FRIDAY;
            default:
                return Calendar.SATURDAY;
        }
    }

    @Transactional(readOnly = true)
    public Inicio ayer(Inicio inicio) {
        Inicio ayer = new Inicio();
        Integer anio = new Integer(inicio.getAnio());
        String trimestre = inicio.getTrimestre();
        String leccion = inicio.getLeccion();
        String dia = inicio.getDia();
        if (StringUtils.isBlank(dia)) {
            dia = obtieneDia(new GregorianCalendar(Locale.ENGLISH).get(Calendar.DAY_OF_WEEK));
        }
        Trimestre t = trimestreService.obtiene(anio + trimestre);
        if (t != null) {
            try {
                Calendar cal = new GregorianCalendar(Locale.ENGLISH);
                cal.setTime(t.getInicia());
                cal.add(Calendar.SECOND, 1);
                cal.set(Calendar.DAY_OF_WEEK, obtieneDia(dia));
                int weeks = ((Long) nf.parse(leccion.substring(1))).intValue();
                if (dia.equals("sabado")) {
                    weeks--;
                }
                cal.add(Calendar.WEEK_OF_YEAR, weeks);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                Date hoy = cal.getTime();

                ayer.setHoy(hoy);

                t = trimestreService.obtiene(hoy);

                DateTime a = new DateTime(t.getInicia());
                DateTime b = new DateTime(hoy);

                Weeks c = Weeks.weeksBetween(a, b);
                weeks = c.getWeeks();
                weeks++;
                leccion = "l" + dosDigitos.format(weeks);
                ayer.setAnio(t.getNombre().substring(0, 4));
                ayer.setTrimestre(t.getNombre().substring(4));
                ayer.setLeccion(leccion);
                ayer.setDia(obtieneDia(cal.get(Calendar.DAY_OF_WEEK)));

                return ayer;
            } catch (ParseException e) {
                log.error("No pude poner la fecha de ayer", e);
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Inicio manana(Inicio inicio) {
        Inicio manana = new Inicio();
        Integer anio = new Integer(inicio.getAnio());
        String trimestre = inicio.getTrimestre();
        String leccion = inicio.getLeccion();
        String dia = inicio.getDia();
        if (StringUtils.isBlank(dia)) {
            dia = obtieneDia(new GregorianCalendar(Locale.ENGLISH).get(Calendar.DAY_OF_WEEK));
        }
        Trimestre t = trimestreService.obtiene(anio + trimestre);
        if (t != null) {
            try {
                Calendar cal = new GregorianCalendar(Locale.ENGLISH);
                cal.setTime(t.getInicia());
                cal.add(Calendar.SECOND, 1);
                cal.set(Calendar.DAY_OF_WEEK, obtieneDia(dia));
                int weeks = ((Long) nf.parse(leccion.substring(1))).intValue();
                if (dia.equals("sabado")) {
                    weeks--;
                }
                cal.add(Calendar.WEEK_OF_YEAR, weeks);
                cal.add(Calendar.DAY_OF_MONTH, +1);
                Date hoy = cal.getTime();

                manana.setHoy(hoy);

                t = trimestreService.obtiene(hoy);

                DateTime a = new DateTime(t.getInicia());
                DateTime b = new DateTime(hoy);

                Weeks c = Weeks.weeksBetween(a, b);
                weeks = c.getWeeks();
                weeks++;
                leccion = "l" + dosDigitos.format(weeks);
                manana.setAnio(t.getNombre().substring(0, 4));
                manana.setTrimestre(t.getNombre().substring(4));
                manana.setLeccion(leccion);
                manana.setDia(obtieneDia(cal.get(Calendar.DAY_OF_WEEK)));

                return manana;
            } catch (ParseException e) {
                log.error("No pude poner la fecha de manana", e);
            }
        }
        return null;
    }
}
