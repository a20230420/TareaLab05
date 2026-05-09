package com.example.tarealab05.Controller;

import com.example.tarealab05.Entity.Product;
import com.example.tarealab05.Repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/productos")
public class ProductController {

    private final ProductRepository productoRepo;
    public ProductController(ProductRepository productoRepo) {
        this.productoRepo = productoRepo;
    }

    // ── LISTAR ────────────────────────────────────────────────
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("lista", productoRepo.findAll());
        return "productos/lista";
    }

    // ── FORMULARIO NUEVO ──────────────────────────────────────
    @GetMapping("/nuevo")
    public String nuevo(@ModelAttribute("product") Product product) {
        return "productos/formulario";
    }

    // ── FORMULARIO EDITAR ─────────────────────────────────────
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id,
                         @ModelAttribute("product") Product product,
                         Model model,
                         RedirectAttributes ra) {

        Optional<Product> opt = productoRepo.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/productos";
        }

        model.addAttribute("product", opt.get());
        return "productos/formulario";
    }

    // ── GUARDAR ───────────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("product") Product product,
                          BindingResult br,
                          RedirectAttributes ra) {

        // Validar nombre único
        if (!br.hasFieldErrors("name") && product.getName() != null) {
            boolean existe = product.getId() == null
                    ? productoRepo.existsByName(product.getName())
                    : productoRepo.existsByNameAndIdNot(
                    product.getName(), product.getId());
            if (existe) {
                br.rejectValue("name", "error.name",
                        "Ya existe un producto con ese nombre");
            }
        }

        if (br.hasErrors()) return "productos/formulario";

        boolean esNuevo = product.getId() == null;
        productoRepo.save(product);
        ra.addFlashAttribute("exito", esNuevo
                ? "Producto registrado correctamente."
                : "Producto actualizado correctamente.");
        return "redirect:/productos";
    }

    // ── ELIMINAR ──────────────────────────────────────────────
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        productoRepo.deleteById(id);
        ra.addFlashAttribute("exito", "Producto eliminado.");
        return "redirect:/productos";
    }
}