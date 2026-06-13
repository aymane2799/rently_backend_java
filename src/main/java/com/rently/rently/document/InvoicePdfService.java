package com.rently.rently.document;

import com.rently.rently.agency.Agency;
import com.rently.rently.agency.AgencyRepository;
import com.rently.rently.catalog.PublicCatalogService;
import com.rently.rently.catalog.models.Model;
import com.rently.rently.multitenancy.TenantContext;
import com.rently.rently.reservation.payment.Payment;
import com.rently.rently.reservation.payment.PaymentRepository;
import com.rently.rently.reservation.reservation.Reservation;
import com.rently.rently.reservation.reservation.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final AgencyRepository agencyRepository;
    private final PublicCatalogService catalogService;
    private final PdfGenerationService pdfGenerationService;

    @Value("${document.storage.path:./documents}")
    private String storagePath;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Async("documentExecutor")
    public void generateAsync(String reservationId, String tenantSlug) {
        TenantContext.setTenantId(tenantSlug);
        try {
            Reservation reservation = reservationRepository.findByIdWithDetails(reservationId)
                    .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + reservationId));

            Payment payment = paymentRepository.findByReservationId(reservationId).orElse(null);
            Agency agency = agencyRepository.findBySlug(tenantSlug).orElse(null);
            Model model = catalogService.getModelWithBrand(reservation.getVehicle().getModelId());

            String html = buildHtml(reservation, payment, agency, model);
            byte[] pdfBytes = pdfGenerationService.generateFromHtml(html);

            Path dir = Paths.get(storagePath, "invoices");
            Files.createDirectories(dir);
            Path filePath = dir.resolve("invoice-" + reservationId + ".pdf");
            Files.write(filePath, pdfBytes);

            reservation.setInvoiceUrl(filePath.toString());
            reservationRepository.save(reservation);

            log.info("Invoice PDF generated for reservation {}", reservationId);
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for reservation {}: {}", reservationId, e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }

    private String buildHtml(Reservation r, Payment p, Agency agency, Model model) {
        String agencyName = agency != null ? agency.getName() : "N/A";
        String agencyRc = agency != null ? agency.getRcNumber() : "N/A";
        String agencyIce = agency != null ? agency.getIceNumber() : "N/A";
        String agencyIf = agency != null && agency.getIfNumber() != null ? agency.getIfNumber() : "N/A";
        String agencyCity = agency != null ? agency.getCity() : "N/A";
        String agencyPhone = agency != null ? agency.getPhone() : "N/A";

        var customer = r.getCustomer();
        var vehicle = r.getVehicle();
        String brandName = model.getBrand() != null ? model.getBrand().getName() : "N/A";

        long rentalDays = ChronoUnit.DAYS.between(r.getStartDate().toLocalDate(), r.getEndDate().toLocalDate());
        if (rentalDays == 0) rentalDays = 1;

        String dailyRate = vehicle.getDailyBaseRate() != null ? vehicle.getDailyBaseRate().toPlainString() : "N/A";
        String totalAmount = r.getTotalAmount().toPlainString();

        BigDecimal deposit = p != null ? p.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal cash = p != null ? p.getCashAdvanced() : BigDecimal.ZERO;
        BigDecimal remaining = r.getTotalAmount().subtract(cash);

        String depositStatus = p != null ? p.getDepositStatus().name() : "N/A";

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>"
                + "body{font-family:Arial,Helvetica,sans-serif;font-size:9pt;color:#222;padding:25px}"
                + "h1{font-size:16pt;color:#1a3a5c;text-align:center;margin:0 0 4px}"
                + "h2{font-size:10pt;background:#1a3a5c;color:#fff;padding:4px 8px;margin:16px 0 6px}"
                + ".center{text-align:center}.sub{font-size:8pt;color:#555;text-align:center}"
                + "table{width:100%;border-collapse:collapse;margin-bottom:6px}"
                + "td,th{padding:4px 7px;border:1px solid #ccc}"
                + "th{background:#f4f4f4;font-weight:bold;text-align:left}"
                + "td.lbl{width:36%;background:#f4f4f4;font-weight:bold}"
                + ".total-row td{font-weight:bold;background:#e8f0fe}"
                + ".footer{margin-top:20px;text-align:center;font-size:7.5pt;color:#888;border-top:1px solid #ccc;padding-top:6px}"
                + ".right{text-align:right}"
                + "</style></head><body>"

                + "<h1>FACTURE DE LOCATION</h1>"
                + "<p class=\"sub\">" + agencyName + " | RC: " + agencyRc + " | ICE: " + agencyIce + " | IF: " + agencyIf + "<br/>"
                + agencyCity + " | Tel: " + agencyPhone + "</p>"
                + "<p class=\"sub\" style=\"margin-top:4px\">N° Facture: <strong>" + r.getId() + "</strong> | Date: " + r.getEndDate().format(DT_FMT) + "</p>"

                + "<h2>INFORMATIONS CLIENT</h2>"
                + "<table>"
                + "<tr><td class=\"lbl\">Nom &amp; Prénom</td><td>" + customer.getFirstName() + " " + customer.getLastName() + "</td>"
                + "<td class=\"lbl\">Téléphone</td><td>" + customer.getPhone() + "</td></tr>"
                + "<tr><td class=\"lbl\">Pièce d'identité</td><td>" + customer.getIdType().name() + " — " + customer.getIdNumber() + "</td>"
                + "<td class=\"lbl\">Permis</td><td>" + customer.getDriverLicenseCode() + "</td></tr>"
                + "</table>"

                + "<h2>DÉTAIL LOCATION</h2>"
                + "<table>"
                + "<tr><th>Description</th><th>Qté</th><th>P.U.</th><th class=\"right\">Montant</th></tr>"
                + "<tr><td>" + brandName + " " + model.getName() + " — " + vehicle.getLicensePlate() + "</td>"
                + "<td>" + rentalDays + " j</td>"
                + "<td>" + dailyRate + " MAD/j</td>"
                + "<td class=\"right\">" + totalAmount + " MAD</td></tr>"
                + "<tr class=\"total-row\"><td colspan=\"3\">TOTAL</td><td class=\"right\">" + totalAmount + " MAD</td></tr>"
                + "</table>"

                + "<h2>RÈGLEMENT</h2>"
                + "<table>"
                + "<tr><td class=\"lbl\">Avance en espèces</td><td>" + cash.toPlainString() + " MAD</td>"
                + "<td class=\"lbl\">Reste à régler</td><td>" + remaining.toPlainString() + " MAD</td></tr>"
                + "<tr><td class=\"lbl\">Type de caution</td><td>" + (p != null ? p.getDepositType().name() : "N/A") + "</td>"
                + "<td class=\"lbl\">Montant caution</td><td>" + deposit.toPlainString() + " MAD</td></tr>"
                + "<tr><td class=\"lbl\">Statut caution</td><td colspan=\"3\">" + depositStatus + "</td></tr>"
                + "</table>"

                + "<div class=\"footer\">Merci pour votre confiance — KiraDrive | Plateforme de gestion de location de voitures</div>"
                + "</body></html>";
    }
}
