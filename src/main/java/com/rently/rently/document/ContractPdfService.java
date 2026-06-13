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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractPdfService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final AgencyRepository agencyRepository;
    private final PublicCatalogService catalogService;
    private final PdfGenerationService pdfGenerationService;

    @Value("${document.storage.path:./documents}")
    private String storagePath;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

            Path dir = Paths.get(storagePath, "contracts");
            Files.createDirectories(dir);
            Path filePath = dir.resolve("contract-" + reservationId + ".pdf");
            Files.write(filePath, pdfBytes);

            reservation.setContractUrl(filePath.toString());
            reservationRepository.save(reservation);

            log.info("Contract PDF generated for reservation {}", reservationId);
        } catch (Exception e) {
            log.error("Failed to generate contract PDF for reservation {}: {}", reservationId, e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }

    private String buildHtml(Reservation r, Payment p, Agency agency, Model model) {
        String agencyName = agency != null ? agency.getName() : "N/A";
        String agencyRc = agency != null ? agency.getRcNumber() : "N/A";
        String agencyIce = agency != null ? agency.getIceNumber() : "N/A";
        String agencyIf = agency != null && agency.getIfNumber() != null ? agency.getIfNumber() : "N/A";
        String agencyPatent = agency != null && agency.getPatent() != null ? agency.getPatent() : "N/A";
        String agencyCity = agency != null ? agency.getCity() : "N/A";
        String agencyAddress = agency != null && agency.getAddress() != null ? agency.getAddress() : "";
        String agencyPhone = agency != null ? agency.getPhone() : "N/A";
        String agencyEmail = agency != null ? agency.getEmail() : "N/A";

        var customer = r.getCustomer();
        var vehicle = r.getVehicle();

        String brandName = model.getBrand() != null ? model.getBrand().getName() : "N/A";
        String modelName = model.getName();
        String vehicleYear = vehicle.getYear() != null ? vehicle.getYear().toString() : "N/A";
        String vehicleColor = vehicle.getColor() != null ? vehicle.getColor() : "N/A";
        String fuelType = vehicle.getFuelType() != null ? vehicle.getFuelType().name() : "N/A";
        String transmission = vehicle.getTransmission() != null ? vehicle.getTransmission().name() : "N/A";
        String dailyRate = vehicle.getDailyBaseRate() != null ? vehicle.getDailyBaseRate().toPlainString() + " MAD" : "N/A";

        String pickupHubName = r.getPickupHub().getName() + " — " + r.getPickupHub().getCity();
        String returnHubName = r.getReturnHub().getName() + " — " + r.getReturnHub().getCity();

        String depositInfo = p != null ? p.getDepositType().name() + " — " + p.getDepositAmount().toPlainString() + " MAD" : "N/A";
        String cashAdvanced = p != null ? p.getCashAdvanced().toPlainString() + " MAD" : "N/A";

        String signatureSection = r.getSignatureBase64() != null
                ? "<div class=\"sig-box\"><p><strong>Signature numérique du client</strong></p>"
                  + "<img src=\"data:image/png;base64," + r.getSignatureBase64() + "\" class=\"sig-img\"/></div>"
                : "<div class=\"sig-box\"><p><strong>Signature du client</strong></p><div class=\"sig-line\"></div></div>";

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>"
                + "body{font-family:Arial,Helvetica,sans-serif;font-size:9pt;color:#222;padding:25px}"
                + "h1{font-size:16pt;color:#1a3a5c;text-align:center;margin:0 0 4px}"
                + "h2{font-size:10pt;background:#1a3a5c;color:#fff;padding:4px 8px;margin:16px 0 6px}"
                + ".center{text-align:center}.sub{font-size:8pt;color:#555;text-align:center}"
                + "table{width:100%;border-collapse:collapse;margin-bottom:6px}"
                + "td{padding:4px 7px;border:1px solid #ccc}"
                + "td.lbl{width:36%;background:#f4f4f4;font-weight:bold}"
                + ".total-row td{font-weight:bold;background:#e8f0fe}"
                + ".footer{margin-top:20px;text-align:center;font-size:7.5pt;color:#888;border-top:1px solid #ccc;padding-top:6px}"
                + ".sig-box{border:1px solid #ccc;padding:10px;min-height:70px;margin-top:10px}"
                + ".sig-img{max-width:160px;max-height:60px}"
                + ".sig-line{border-bottom:1px solid #999;height:50px;margin-top:8px}"
                + "</style></head><body>"

                + "<h1>CONTRAT DE LOCATION</h1>"
                + "<p class=\"sub\">" + agencyName + " | RC: " + agencyRc + " | ICE: " + agencyIce + " | IF: " + agencyIf + " | Patent: " + agencyPatent + "<br/>"
                + agencyAddress + (agencyAddress.isEmpty() ? "" : ", ") + agencyCity + " | Tel: " + agencyPhone + " | " + agencyEmail + "</p>"
                + "<p class=\"sub\" style=\"margin-top:4px\">N° Contrat: <strong>" + r.getId() + "</strong></p>"

                + "<h2>INFORMATIONS CLIENT</h2>"
                + "<table>"
                + "<tr><td class=\"lbl\">Nom &amp; Prénom</td><td>" + customer.getFirstName() + " " + customer.getLastName() + "</td>"
                + "<td class=\"lbl\">Téléphone</td><td>" + customer.getPhone() + "</td></tr>"
                + "<tr><td class=\"lbl\">Type pièce d'identité</td><td>" + customer.getIdType().name() + "</td>"
                + "<td class=\"lbl\">N° pièce d'identité</td><td>" + customer.getIdNumber() + "</td></tr>"
                + "<tr><td class=\"lbl\">Permis de conduire</td><td>" + customer.getDriverLicenseCode() + "</td>"
                + "<td class=\"lbl\">Email</td><td>" + (customer.getEmail() != null ? customer.getEmail() : "N/A") + "</td></tr>"
                + "<tr><td class=\"lbl\">Adresse</td><td colspan=\"3\">" + (customer.getAddress() != null ? customer.getAddress() : "N/A") + "</td></tr>"
                + "</table>"

                + "<h2>INFORMATIONS VÉHICULE</h2>"
                + "<table>"
                + "<tr><td class=\"lbl\">Marque / Modèle</td><td>" + brandName + " " + modelName + "</td>"
                + "<td class=\"lbl\">Immatriculation</td><td>" + vehicle.getLicensePlate() + "</td></tr>"
                + "<tr><td class=\"lbl\">Année</td><td>" + vehicleYear + "</td>"
                + "<td class=\"lbl\">Couleur</td><td>" + vehicleColor + "</td></tr>"
                + "<tr><td class=\"lbl\">Carburant</td><td>" + fuelType + "</td>"
                + "<td class=\"lbl\">Transmission</td><td>" + transmission + "</td></tr>"
                + "<tr><td class=\"lbl\">Tarif journalier</td><td colspan=\"3\">" + dailyRate + "</td></tr>"
                + "</table>"

                + "<h2>PÉRIODE DE LOCATION</h2>"
                + "<table>"
                + "<tr><td class=\"lbl\">Date de départ</td><td>" + r.getStartDate().format(DT_FMT) + "</td>"
                + "<td class=\"lbl\">Hub de départ</td><td>" + pickupHubName + "</td></tr>"
                + "<tr><td class=\"lbl\">Date de retour</td><td>" + r.getEndDate().format(DT_FMT) + "</td>"
                + "<td class=\"lbl\">Hub de retour</td><td>" + returnHubName + "</td></tr>"
                + "<tr class=\"total-row\"><td class=\"lbl\">Montant total</td><td colspan=\"3\">" + r.getTotalAmount().toPlainString() + " MAD</td></tr>"
                + "</table>"

                + "<h2>PAIEMENT ET CAUTION</h2>"
                + "<table>"
                + "<tr><td class=\"lbl\">Avance en espèces</td><td>" + cashAdvanced + "</td>"
                + "<td class=\"lbl\">Type de caution</td><td>" + depositInfo + "</td></tr>"
                + (p != null && p.getBankTransferReference() != null
                        ? "<tr><td class=\"lbl\">Réf. virement</td><td colspan=\"3\">" + p.getBankTransferReference() + "</td></tr>"
                        : "")
                + (p != null && p.getChequeNumber() != null
                        ? "<tr><td class=\"lbl\">N° chèque</td><td colspan=\"3\">" + p.getChequeNumber() + "</td></tr>"
                        : "")
                + "</table>"

                + "<h2>SIGNATURE</h2>"
                + signatureSection

                + "<div class=\"footer\">Statut contrat: " + r.getContractStatus().name()
                + " | Créé le: " + r.getCreatedAt().toString().substring(0, 10)
                + " | KiraDrive — Plateforme de gestion de location de voitures</div>"
                + "</body></html>";
    }
}
