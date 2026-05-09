package com.example.tarealab05.Controller;

import com.example.tarealab05.Entity.Customer;
import com.example.tarealab05.Repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/clientes")
public class CustomerController {


    private final CustomerRepository customerRepo;
    public CustomerController(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    // ── LISTAR ────────────────────────────────────────────────
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("lista", customerRepo.findAll());
        return "clientes/lista";
    }

    // ── FORMULARIO NUEVO ──────────────────────────────────────
    @GetMapping("/nuevo")
    public String nuevo(@ModelAttribute("customer") Customer customer) {
        return "clientes/formulario";
    }

    // ── FORMULARIO EDITAR ─────────────────────────────────────
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id,
                         @ModelAttribute("customer") Customer customer,
                         Model model,
                         RedirectAttributes ra) {

        Optional<Customer> opt = customerRepo.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Cliente no encontrado.");
            return "redirect:/clientes";
        }

        model.addAttribute("customer", opt.get());
        return "clientes/formulario";
    }

    // ── GUARDAR ───────────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("customer") Customer customer,
                          BindingResult br,
                          RedirectAttributes ra) {

        // Validación DNI/RUC según tipo
        if (!br.hasFieldErrors("document")
                && customer.getDocumentType() != null
                && !customer.getDocumentType().isBlank()) {

            if (customer.getDocumentType().equals("DNI") &&
                    !customer.getDocument().matches("\\d{8}")) {
                br.rejectValue("document", "error.document",
                        "DNI debe tener exactamente 8 dígitos numéricos");

            } else if (customer.getDocumentType().equals("RUC") &&
                    !customer.getDocument().matches("\\d{11}")) {
                br.rejectValue("document", "error.document",
                        "RUC debe tener exactamente 11 dígitos numéricos");
            }
        }

        // Validar documento único
        if (!br.hasFieldErrors("document") && customer.getDocument() != null) {
            boolean existe = customer.getId() == null
                    ? customerRepo.existsByDocument(customer.getDocument())
                    : customerRepo.existsByDocumentAndIdNot(
                    customer.getDocument(), customer.getId());
            if (existe) {
                br.rejectValue("document", "error.document",
                        "Ya existe un cliente con ese número de documento");
            }
        }

        if (br.hasErrors()) return "clientes/formulario";

        boolean esNuevo = customer.getId() == null;
        customerRepo.save(customer);
        ra.addFlashAttribute("exito", esNuevo
                ? "Cliente registrado correctamente."
                : "Cliente actualizado correctamente.");
        return "redirect:/clientes";
    }

    // ── ELIMINAR ──────────────────────────────────────────────
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        customerRepo.deleteById(id);
        ra.addFlashAttribute("exito", "Cliente eliminado.");
        return "redirect:/clientes";
    }
}
