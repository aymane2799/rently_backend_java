package com.rently.rently.billing;

import com.rently.rently.agency.Agency;
import com.rently.rently.agency.AgencyRepository;
import com.rently.rently.document.PdfGenerationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionInvoicePdfService {

    private final SubscriptionRepository subscriptionRepository;
    private final AgencyRepository agencyRepository;
    private final PdfGenerationService pdfGenerationService;

    @Value("${document.storage.path:./documents}")
    private String storagePath;

    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Async("documentExecutor")
    public void generateAsync(String subscriptionId) {
        log.info("Starting subscription invoice PDF generation for subscription {}", subscriptionId);
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new EntityNotFoundException("Subscription not found: " + subscriptionId));

            Agency agency = agencyRepository.findBySlug(subscription.getAgencySlug()).orElse(null);
            String html = buildHtml(subscription, agency);
            byte[] pdfBytes = pdfGenerationService.generateFromHtml(html);

            Path dir = Paths.get(storagePath, "subscription-invoices");
            Files.createDirectories(dir);
            Path filePath = dir.resolve("sub-invoice-" + subscriptionId + ".pdf");
            Files.write(filePath, pdfBytes);

            subscription.setInvoiceUrl(filePath.toString());
            subscriptionRepository.save(subscription);

            log.info("Subscription invoice PDF generated for subscription {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to generate subscription invoice PDF for subscription {}: {}", subscriptionId, e.getMessage(), e);
        }
    }

    private String buildHtml(Subscription sub, Agency agency) {
        String agencyName = agency != null ? agency.getName() : sub.getAgencySlug();
        String agencyRc = agency != null ? agency.getRcNumber() : "N/A";
        String agencyIce = agency != null ? agency.getIceNumber() : "N/A";
        String agencyIf = agency != null && agency.getIfNumber() != null ? agency.getIfNumber() : "N/A";
        String agencyCity = agency != null ? agency.getCity() : "N/A";
        String agencyPhone = agency != null ? agency.getPhone() : "N/A";

        SubscriptionPlan plan = sub.getPlan();
        String planName = plan != null ? plan.getDisplayName() : "N/A";
        String paidAt = sub.getPaidAt() != null
                ? sub.getPaidAt().atZone(ZoneId.systemDefault()).toLocalDate().format(D_FMT)
                : "N/A";

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>"
                + "body{font-family:Arial,Helvetica,sans-serif;font-size:9pt;color:#222;padding:25px}"
                + "h1{font-size:16pt;color:#1a3a5c;text-align:center;margin:0 0 4px}"
                + "h2{font-size:10pt;background:#1a3a5c;color:#fff;padding:4px 8px;margin:16px 0 6px}"
                + ".sub{font-size:8pt;color:#555;text-align:center}"
                + "table{width:100%;border-collapse:collapse;margin-bottom:6px}"
                + "td,th{padding:4px 7px;border:1px solid #ccc}"
                + "th{background:#f4f4f4;font-weight:bold;text-align:left}"
                + "td.lbl{width:36%;background:#f4f4f4;font-weight:bold}"
                + ".total-row td{font-weight:bold;background:#e8f0fe}"
                + ".footer{margin-top:20px;text-align:center;font-size:7.5pt;color:#888;border-top:1px solid #ccc;padding-top:6px}"
                + ".right{text-align:right}"
                + "</style></head><body>"

                + "<h1>FACTURE D'ABONNEMENT</h1>"
                + "<p class=\"sub\">" + agencyName + " | RC: " + agencyRc + " | ICE: " + agencyIce + " | IF: " + agencyIf + "<br/>"
                + agencyCity + " | Tel: " + agencyPhone + "</p>"
                + "<p class=\"sub\" style=\"margin-top:4px\">N° Abonnement: <strong>" + sub.getId() + "</strong></p>"

                + "<h2>DÉTAIL ABONNEMENT</h2>"
                + "<table>"
                + "<tr><th>Description</th><th class=\"right\">Montant</th></tr>"
                + "<tr><td>Abonnement " + planName + " (" + sub.getStartDate().format(D_FMT) + " — " + sub.getEndDate().format(D_FMT) + ")</td>"
                + "<td class=\"right\">" + sub.getAmountDue().toPlainString() + " MAD</td></tr>"
                + "<tr class=\"total-row\"><td>TOTAL</td><td class=\"right\">" + sub.getAmountDue().toPlainString() + " MAD</td></tr>"
                + "</table>"

                + "<h2>RÈGLEMENT</h2>"
                + "<table>"
                + "<tr><td class=\"lbl\">Mode de paiement</td><td>" + (sub.getPaymentMode() != null ? sub.getPaymentMode() : "N/A") + "</td>"
                + "<td class=\"lbl\">Date de paiement</td><td>" + paidAt + "</td></tr>"
                + "<tr><td class=\"lbl\">Statut</td><td colspan=\"3\">" + sub.getStatus().name() + "</td></tr>"
                + "</table>"

                + "<div class=\"footer\">KiraDrive — Plateforme SaaS de gestion de location de voitures</div>"
                + "</body></html>";
    }
}
