package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.PaymentRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Receipt endpoints for customers.
 * Currently supports colorful Booking Confirmation receipt as downloadable HTML.
 */
@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<byte[]> bookingReceipt(@PathVariable Integer bookingId, Authentication auth){
        try {
            Optional<Booking> opt = bookingRepository.findById(bookingId);
            if (opt.isEmpty()) return err("Booking not found", HttpStatus.NOT_FOUND);
            Booking b = opt.get();

            if (!owns(auth, b)) return err("Forbidden", HttpStatus.FORBIDDEN);

            // With open-in-view disabled, avoid lazy-loading Customer.user; fetch User explicitly
            String customerName = resolveUserName(b);
            String item = b.getVehicle()!=null
                    ? safe(() -> b.getVehicle().getMake() + " " + b.getVehicle().getModel())
                    : (b.getVehiclePackage()!=null ? safe(() -> b.getVehiclePackage().getPackageName()) : "");

            String xhtml = "" +
                    "<!DOCTYPE html><html xmlns='http://www.w3.org/1999/xhtml'><head><meta charset='UTF-8'/>"+
                    "<title>Booking Receipt BK-"+b.getBookingId()+"</title>"+
                    colorfulStyles() +
                    "</head><body>"+
                    "<div class='receipt-card'>"+
                    "  <div class='header'>"+
                    "    <div class='brand'><span class='brand-icon'>🚗</span> <span>VeloRent</span></div>"+
                    "    <div class='tag success'>BOOKING CONFIRMED</div>"+
                    "  </div>"+
                    "  <div class='meta'>"+
                    "    <div><strong>Receipt No</strong><span>BK-"+b.getBookingId()+"</span></div>"+
                    "    <div><strong>Date</strong><span>"+(b.getCreatedAt()!=null?b.getCreatedAt().format(DT):"-")+"</span></div>"+
                    "  </div>"+
                    "  <div class='grid'>"+
                    "    <div><label>Customer</label><div>"+esc(customerName)+"</div></div>"+
                    "    <div><label>Item</label><div>"+esc(item)+"</div></div>"+
                    "    <div><label>Start</label><div>"+(b.getStartDate()!=null?b.getStartDate().format(DT):"-")+"</div></div>"+
                    "    <div><label>End</label><div>"+(b.getEndDate()!=null?b.getEndDate().format(DT):"-")+"</div></div>"+
                    "    <div><label>Status</label><div>"+esc(b.getBookingStatus())+"</div></div>"+
                    "    <div><label>Total</label><div>"+(b.getTotalCost()!=null?b.getTotalCost().toPlainString():"0.00")+"</div></div>"+
                    "  </div>"+
                    "  <div class='footer'>Thank you for choosing <b>VeloRent</b>. Drive safe and enjoy your ride!</div>"+
                    "</div>"+
                    "</body></html>";

            // Render PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(baos);
            byte[] pdf = baos.toByteArray();

            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_PDF);
            h.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"booking-receipt-"+b.getBookingId()+".pdf\"");
            h.setContentLength(pdf.length);
            return new ResponseEntity<>(pdf, h, HttpStatus.OK);
        } catch (Exception ex){
            return err("Failed to generate receipt", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment/{bookingId}")
    public ResponseEntity<byte[]> paymentReceipt(@PathVariable Integer bookingId, Authentication auth){
        try {
            Optional<Booking> opt = bookingRepository.findById(bookingId);
            if (opt.isEmpty()) return err("Booking not found", HttpStatus.NOT_FOUND);
            Booking b = opt.get();

            if (!owns(auth, b)) return err("Forbidden", HttpStatus.FORBIDDEN);

            Payment p = paymentRepository.findByBooking(b);
            if (p == null || p.getPaymentStatus() == null || !"Completed".equalsIgnoreCase(p.getPaymentStatus())){
                return err("Payment not found or not completed", HttpStatus.NOT_FOUND);
            }

        // Gather fields
    // With open-in-view disabled, avoid lazy-loading Customer.user; fetch User explicitly
    String customerName = resolveUserName(b);
    String customerEmail = resolveUserEmail(b);
        String customerPhone = safe(() -> b.getCustomer().getContactNumber());
        String customerAddress = (safe(() -> b.getCustomer().getAddressStreet()) + ", " +
            safe(() -> b.getCustomer().getAddressCity()) + " " + safe(() -> b.getCustomer().getAddressPostalCode())).replaceAll(", ", ", ").trim();
        String carDetails = b.getVehicle()!=null
            ? safe(() -> b.getVehicle().getMake() + " " + b.getVehicle().getModel() + " ("+b.getVehicle().getYear()+")")
            : (b.getVehiclePackage()!=null ? safe(() -> b.getVehiclePackage().getPackageName()) : "");
        String carNumber = b.getVehicle()!=null ? safe(() -> b.getVehicle().getRegistrationNumber()) : "-";
        java.time.Duration dur = (b.getStartDate()!=null && b.getEndDate()!=null) ? java.time.Duration.between(b.getStartDate(), b.getEndDate()) : java.time.Duration.ZERO;
        long hours = dur.toHours(); long days = hours/24; long remH = hours%24;
        String durationText = (days>0? days+" day(s) ":"") + (remH>0? remH+" hour(s)":"");
        String rentPer = b.getVehicle()!=null && b.getVehicle().getRentalRatePerDay()!=null ? b.getVehicle().getRentalRatePerDay().toPlainString()+" /day" : "-";
        String subTotal = b.getTotalCost()!=null ? b.getTotalCost().toPlainString() : (p.getAmount()!=null ? p.getAmount().toPlainString() : "0.00");
        java.math.BigDecimal bdSub = new java.math.BigDecimal(subTotal);
        java.math.BigDecimal bdPaid = p.getAmount()!=null ? p.getAmount() : bdSub;
        java.math.BigDecimal bdDisc = bdSub.subtract(bdPaid);
        if (bdDisc.compareTo(java.math.BigDecimal.ZERO) < 0) bdDisc = java.math.BigDecimal.ZERO;
        String discount = bdDisc.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        String grandTotal = bdPaid.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();

        String xhtml = "" +
            "<!DOCTYPE html><html xmlns='http://www.w3.org/1999/xhtml'><head><meta charset='UTF-8'/>"+
            "<title>Payment Receipt PMT-"+p.getPaymentId()+"</title>"+
            "<style>body{background:#fafafa;font-family:Segoe UI,Arial,sans-serif;color:#1f2937;margin:0;padding:24px}"+
            ".sheet{max-width:860px;margin:0 auto;background:#fff;border:1px solid #e5e7eb;border-radius:12px;box-shadow:0 8px 28px rgba(0,0,0,.06);overflow:hidden}"+
            ".header{display:flex;justify-content:space-between;align-items:flex-start;padding:22px 24px;border-bottom:2px solid #0ea5e9;background:#f0f9ff}"+
            ".brand{font-weight:800;font-size:22px;color:#0f172a} .brand small{display:block;color:#334155;font-weight:600;font-size:12px}"+
            ".meta{text-align:right;font-size:13px;color:#334155} .meta div{margin:4px 0}"+
            ".row{display:flex;gap:18px;padding:18px 24px;border-bottom:1px solid #eef2f7}"+
            ".col{flex:1} .title{font-weight:800;color:#0ea5e9;margin:0 0 6px;font-size:16px} .kv div{margin:6px 0} .kv label{display:inline-block;width:120px;color:#64748b} .kv span{color:#111827}"+
            "table{width:calc(100% - 48px);margin:0 24px 12px;border-collapse:collapse;font-size:14px}"+
            "th,td{border:1px solid #e5e7eb;padding:10px 8px;text-align:left} th{background:#0ea5e9;color:#fff;font-weight:700}"+
            ".totals{width:calc(100% - 48px);margin:8px 24px 20px} .totals .r{display:flex;justify-content:flex-end;margin:6px 0} .totals label{min-width:160px;text-align:right;margin-right:12px;color:#374151} .totals span{min-width:140px;text-align:right;font-weight:800}"+
            ".footer{display:flex;justify-content:space-between;align-items:center;padding:14px 24px;background:#f0f9ff;border-top:1px solid #e5e7eb;color:#0f172a}"+
            "</style></head><body>"+
            "<div class='sheet'>"+
            "  <div class='header'>"+
            "    <div class='brand'>VeloRent<small>Car Rental Services</small><small>3917 Walnut Creek, CA 945</small><small>+1 (555) 923-4545</small></div>"+
            "    <div class='meta'>"+
            "      <div><b>Payment Receipt</b></div>"+
            "      <div>Receipt No: PMT-"+p.getPaymentId()+"</div>"+
            "      <div>Date: "+(p.getPaymentDate()!=null?p.getPaymentDate().format(DT):"-")+"</div>"+
            "    </div>"+
            "  </div>"+
            "  <div class='row'>"+
            "    <div class='col'>"+
            "      <div class='title'>Billed To</div>"+
            "      <div class='kv'>"+
            "        <div><label>Name:</label><span>"+esc(customerName)+"</span></div>"+
            "        <div><label>Email:</label><span>"+esc(customerEmail)+"</span></div>"+
            "        <div><label>Phone:</label><span>"+esc(customerPhone)+"</span></div>"+
            "        <div><label>Address:</label><span>"+esc(customerAddress)+"</span></div>"+
            "      </div>"+
            "    </div>"+
            "    <div class='col'>"+
            "      <div class='title'>Payment Details</div>"+
            "      <div class='kv'>"+
            "        <div><label>Booking ID:</label><span>"+b.getBookingId()+"</span></div>"+
            "        <div><label>Method:</label><span>"+esc(p.getPaymentMethod())+"</span></div>"+
            "        <div><label>Status:</label><span>"+esc(p.getPaymentStatus())+"</span></div>"+
            "        <div><label>Paid Amount:</label><span>"+esc(grandTotal)+"</span></div>"+
            "      </div>"+
            "    </div>"+
            "  </div>"+
            "  <div class='row' style='padding-bottom:10px'>"+
            "    <div class='col' style='flex:1 1 100%'>"+
            "      <div class='title'>Rental Summary</div>"+
            "    </div>"+
            "  </div>"+
            "  <table><thead><tr><th>#</th><th>Description</th><th>Duration</th><th>Rate</th><th style='text-align:right'>Amount</th></tr></thead><tbody>"+
            "    <tr><td>1</td><td>"+esc(carDetails)+" ("+esc(carNumber)+")</td><td>"+esc(durationText)+"</td><td>"+esc(rentPer)+"</td><td style='text-align:right'>"+esc(subTotal)+"</td></tr>"+
            "  </tbody></table>"+
            "  <div class='totals'>"+
            "    <div class='r'><label>Sub Total</label><span>"+esc(subTotal)+"</span></div>"+
            "    <div class='r'><label>Discount</label><span>"+esc(discount)+"</span></div>"+
            "    <div class='r'><label>Total Paid</label><span>"+esc(grandTotal)+"</span></div>"+
            "  </div>"+
            "  <div class='footer'>"+
            "    <div>Thank you, <b>"+esc(customerName)+"</b>, for your payment.</div>"+
            "    <div>VeloRent • www.velorent.example</div>"+
            "  </div>"+
            "</div>"+
            "</body></html>";

            // Render PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(baos);
            byte[] pdf = baos.toByteArray();

            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_PDF);
            h.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payment-receipt-"+b.getBookingId()+".pdf\"");
            h.setContentLength(pdf.length);
            return new ResponseEntity<>(pdf, h, HttpStatus.OK);
        } catch (Exception ex){
            return err("Failed to generate receipt", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean owns(Authentication auth, Booking b){
        try {
            if (auth==null || auth.getName()==null) return false;
            Optional<User> u = userRepository.findByEmail(auth.getName());
            return u.isPresent() && b.getCustomer()!=null && b.getCustomer().getUser()!=null &&
                    u.get().getId()!=null && u.get().getId().equals(b.getCustomer().getUser().getId());
        } catch(Exception e){ return false; }
    }

    private ResponseEntity<byte[]> err(String msg, HttpStatus status){
        byte[] data = ("<html><body>"+esc(msg)+"</body></html>").getBytes(StandardCharsets.UTF_8);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.TEXT_HTML);
        h.setContentLength(data.length);
        return new ResponseEntity<>(data, h, status);
    }

    private static String esc(String s){ return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }

    private static String colorfulStyles(){
        return "<style>body{background:#0e0f12;color:#e8eaed;font-family:Inter,Segoe UI,Arial,sans-serif;margin:0;padding:24px}"+
                ".receipt-card{max-width:800px;margin:0 auto;background:linear-gradient(145deg,#15171c,#0f1115);border:1px solid #2b2f37;border-radius:16px;box-shadow:0 20px 50px rgba(0,0,0,.5);overflow:hidden}"+
                ".header{display:flex;justify-content:space-between;align-items:center;padding:18px 22px;background:linear-gradient(135deg,#1e2026 0%,#15171c 100%);border-bottom:1px solid #2b2f37}"+
                ".brand{display:flex;align-items:center;gap:10px;color:#ffd166;font-weight:800;font-size:1.1rem;letter-spacing:.3px}"+
                ".brand-icon{font-size:20px}"+
                ".tag{padding:6px 10px;border-radius:999px;font-size:.8rem;font-weight:700;letter-spacing:.4px;border:1px solid rgba(255,255,255,.12)}"+
                ".tag.success{background:linear-gradient(135deg,#2ecc71 0%,#27ae60 100%);color:#08130c;border-color:#58d68d}"+
                ".meta{display:grid;grid-template-columns:1fr 1fr;gap:12px;padding:16px 22px;color:#cfd3da;background:linear-gradient(180deg,rgba(255,255,255,.03),transparent)}"+
                ".meta strong{display:block;color:#9aa0a6;font-size:.78rem;font-weight:700;margin-bottom:4px}"+
                ".meta span{font-size:.98rem;color:#e8eaed}"+
                ".grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px;padding:8px 22px 22px}"+
                ".grid label{display:block;color:#9aa0a6;font-size:.78rem;font-weight:700;margin-bottom:6px}"+
                ".grid div{color:#e8eaed;font-size:1rem}"+
                ".footer{padding:14px 22px;border-top:1px solid #2b2f37;background:linear-gradient(180deg,transparent,rgba(255,255,255,.04));color:#ffd166;font-weight:600;letter-spacing:.3px}"+
                "@media(max-width:640px){.grid{grid-template-columns:1fr}.meta{grid-template-columns:1fr}}"+
                "</style>";
    }

    private static String safe(java.util.concurrent.Callable<String> c){
        try { return c.call(); } catch(Exception e){ return ""; }
    }

    // --- Helpers to safely resolve User fields without triggering lazy loads ---
    private String resolveUserName(Booking b){
        try {
            if (b==null || b.getCustomer()==null) return "";
            Long uid = b.getCustomer().getUserId();
            if (uid == null) return "";
            Optional<User> u = userRepository.findById(uid);
            if (u.isPresent()) {
                String fn = u.get().getFirstName()!=null ? u.get().getFirstName() : "";
                String ln = u.get().getLastName()!=null ? u.get().getLastName() : "";
                return (fn + " " + ln).trim();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String resolveUserEmail(Booking b){
        try {
            if (b==null || b.getCustomer()==null) return "";
            Long uid = b.getCustomer().getUserId();
            if (uid == null) return "";
            Optional<User> u = userRepository.findById(uid);
            return u.map(User::getEmail).orElse("");
        } catch (Exception ignored) {}
        return "";
    }
}
