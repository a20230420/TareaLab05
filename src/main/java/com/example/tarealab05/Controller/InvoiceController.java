package com.example.tarealab05.Controller;


import com.example.tarealab05.Entity.Customer;
import com.example.tarealab05.Entity.Invoice;
import com.example.tarealab05.Entity.InvoiceDetail;
import com.example.tarealab05.Entity.Product;
import com.example.tarealab05.Repository.CustomerRepository;
import com.example.tarealab05.Repository.InvoiceDetailRepository;
import com.example.tarealab05.Repository.InvoiceRepository;
import com.example.tarealab05.Repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/comprobantes")
public class InvoiceController {

    private final InvoiceRepository invoiceRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final InvoiceDetailRepository detailRepo;
    public InvoiceController(InvoiceRepository invoiceRepo,
                             CustomerRepository customerRepo,
                             ProductRepository productRepo,
                             InvoiceDetailRepository detailRepo) {
        this.invoiceRepo = invoiceRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.detailRepo = detailRepo;
    }

    // ── LISTAR ────────────────────────────────────────────────
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("lista", invoiceRepo.findAll());
        return "comprobantes/lista";
    }

    // ── FORMULARIO NUEVO ──────────────────────────────────────
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("clientes", customerRepo.findAll());
        model.addAttribute("productos", productRepo.findAll());
        return "comprobantes/formulario";
    }

    // ── GUARDAR ───────────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardar(
            @RequestParam String type,
            @RequestParam Integer customerId,
            @RequestParam String date,
            @RequestParam List<Integer> productIds,
            @RequestParam List<Integer> quantities,
            Model model,
            RedirectAttributes ra) {

        List<String> errores = new ArrayList<>();

        // Cargar cliente
        Customer customer = customerRepo.findById(customerId).orElse(null);
        if (customer == null) {
            errores.add("Cliente no válido.");
        }

        // Validar tipo según documento del cliente
        if (customer != null) {
            if (type.equals("FACTURA") && !customer.getDocumentType().equals("RUC")) {
                errores.add("Factura solo para clientes con RUC.");
            }
            if (type.equals("BOLETA") && !customer.getDocumentType().equals("DNI")) {
                errores.add("Boleta solo para clientes con DNI.");
            }
        }

        // Validar fecha
        LocalDate fecha = null;
        try {
            fecha = LocalDate.parse(date);
            if (fecha.isAfter(LocalDate.now())) {
                errores.add("La fecha no puede ser futura.");
            }
        } catch (Exception e) {
            errores.add("Fecha inválida.");
        }

        // Validar que haya al menos un producto con cantidad > 0
        boolean hayProducto = false;
        for (int qty : quantities) {
            if (qty > 0) { hayProducto = true; break; }
        }
        if (!hayProducto) {
            errores.add("Debe ingresar cantidad mayor a 0 en al menos un producto.");
        }

        // Validar stock por cada producto
        for (int i = 0; i < productIds.size(); i++) {
            int qty = quantities.get(i);
            if (qty > 0) {
                Product p = productRepo.findById(productIds.get(i)).orElse(null);
                if (p != null && qty > p.getStock()) {
                    errores.add("Stock insuficiente para " + p.getName()
                            + " (disponible: " + p.getStock() + ")");
                }
            }
        }

        // Si hay errores volver al formulario
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("clientes", customerRepo.findAll());
            model.addAttribute("productos", productRepo.findAll());
            return "comprobantes/formulario";
        }

        // Crear comprobante
        Invoice invoice = new Invoice();
        invoice.setType(type);
        invoice.setDate(fecha);
        invoice.setCustomer(customer);
        invoiceRepo.save(invoice);

        // Guardar detalles y descontar stock
        for (int i = 0; i < productIds.size(); i++) {
            int qty = quantities.get(i);
            if (qty > 0) {
                Product p = productRepo.findById(productIds.get(i)).orElseThrow();

                InvoiceDetail detail = new InvoiceDetail();
                detail.setInvoice(invoice);
                detail.setProduct(p);
                detail.setQuantity(qty);
                detail.setPrice(p.getPrice());
                detail.setSubtotal(p.getPrice() * qty);
                detailRepo.save(detail);

                // Descontar stock
                p.setStock(p.getStock() - qty);
                productRepo.save(p);
            }
        }

        ra.addFlashAttribute("exito", "Comprobante emitido correctamente.");
        return "redirect:/comprobantes";
    }
}
