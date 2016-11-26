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
package org.davidmendoza.esu.estudia;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpSession;
import org.davidmendoza.esu.inicio.InicioService;
import org.davidmendoza.esu.shared.Inicio;
import org.davidmendoza.esu.shared.Popular;
import org.davidmendoza.esu.shared.Publicacion;
import org.davidmendoza.esu.shared.PublicacionService;
import org.davidmendoza.esu.shared.Trimestre;
import org.davidmendoza.esu.shared.TrimestreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author J. David Mendoza <jdmendoza@swau.edu>
 */
@Controller
@RequestMapping("/estudia")
public class EstudiaController {

    public static final Logger log = LoggerFactory.getLogger(EstudiaController.class);
    
    @Autowired
    private InicioService inicioService;
    @Autowired
    private PublicacionService publicacionService;
    @Autowired
    private TrimestreService trimestreService;

    @GetMapping
    public String index(TimeZone timeZone) {
        Inicio inicio = inicioService.inicio(timeZone);
        if (inicio != null) {
            StringBuilder url = new StringBuilder();
            url.append("redirect:/estudia/");
            url.append(inicio.getAnio()).append("/");
            url.append(inicio.getTrimestre()).append("/");
            url.append(inicio.getLeccion()).append("/");
            url.append(inicio.getDia());
            return url.toString();
        } else {
            return "inicio/trimestre";
        }
    }

    @GetMapping("/{anio}/{trimestre}/{leccion}/{dia}")    
    public String estudia(Model model, @ModelAttribute Inicio inicio, HttpSession session, TimeZone timeZone) {
        log.info("Estudia: {} : {} : {} : {}", inicio.getAnio(), inicio.getTrimestre(), inicio.getLeccion(), inicio.getDia());

        inicio = inicioService.inicio(inicio);

        Inicio hoy = inicioService.inicio(timeZone);
        if (hoy != null && inicio.getAnio().equals(hoy.getAnio())
                && inicio.getTrimestre().equals(hoy.getTrimestre())
                && inicio.getLeccion().equals(hoy.getLeccion())
                && inicio.getDia().equals(hoy.getDia())) {
            inicio.setEsHoy(Boolean.TRUE);
        } else if (hoy != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("/estudia");
            sb.append("/").append(hoy.getAnio());
            sb.append("/").append(hoy.getTrimestre());
            sb.append("/").append(hoy.getLeccion());
            sb.append("/").append(hoy.getDia());
            inicio.setHoyLiga(sb.toString());
            inicio.setEsHoy(Boolean.FALSE);
        }

        Publicacion publicacion = inicio.getPublicacion();
        if (publicacion != null) {
            publicacion.getArticulo().setVistas(publicacionService.agregarVista(publicacion.getArticulo()));

            model.addAttribute("estudia", inicio);
            model.addAttribute("ayer", inicioService.ayer(inicio));
            model.addAttribute("manana", inicioService.manana(inicio));

            return "estudia/leccion";
        } else {
            return "redirect:/";
        }
        
    }
    
    @RequestMapping(value = "/dia", method = RequestMethod.POST, params = {"fecha"})
    public String dia(@RequestParam("fecha") String fecha) {
        log.info("Dia: {}", fecha);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = sdf.parse(fecha);
            Calendar cal = new GregorianCalendar();
            cal.setTime(date);
            cal.add(Calendar.SECOND, 1);
            Inicio inicio = inicioService.inicio(cal);
            StringBuilder url = new StringBuilder();
            url.append("redirect:/estudia/");
            url.append(inicio.getAnio()).append("/");
            url.append(inicio.getTrimestre()).append("/");
            url.append(inicio.getLeccion()).append("/");
            url.append(inicio.getDia());
            return url.toString();
        } catch (ParseException e) {
            log.error("Fecha invalida", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/popular/{posicion}", method = RequestMethod.GET)
    @ResponseBody
    public Map popular(@PathVariable Integer posicion) throws ParseException {
        log.info("Populares Estudia({})", posicion);
        Map resultado = new HashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", new Locale("es"));
        Popular popular = publicacionService.obtieneSiguientePopularEstudia(posicion);
        popular.getPublicacion().getArticulo().setVistas(publicacionService.agregarVista(popular.getPublicacion().getArticulo()));
        Trimestre t = trimestreService.obtiene(popular.getPublicacion().getAnio() + popular.getPublicacion().getTrimestre());
        if (t != null) {
            Calendar cal = new GregorianCalendar(Locale.ENGLISH);
            cal.setTime(t.getInicia());
            cal.add(Calendar.SECOND, 1);
            cal.set(Calendar.DAY_OF_WEEK, obtieneDia(popular.getPublicacion().getDia()));
            NumberFormat nf = NumberFormat.getInstance();
            int weeks = ((Long) nf.parse(popular.getPublicacion().getLeccion().substring(1))).intValue();
            if (popular.getPublicacion().getDia().equals("sabado")) {
                weeks--;
            }
            cal.add(Calendar.WEEK_OF_YEAR, weeks);
            Date hoy = cal.getTime();
            resultado.put("dia", hoy);
            resultado.put("diaString", sdf.format(hoy));
        }
        resultado.put("publicacion", popular.getPublicacion());
        resultado.put("posicion", popular.getId());
        return resultado;
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
}
