package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.VehiclePackageRequest;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.entity.VehiclePackage;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.VehiclePackageRepository;
import Group2.Car.Rental.System.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VehiclePackageService {

    private final VehiclePackageRepository vehiclePackageRepository;
    private final VehicleRepository vehicleRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    public VehiclePackageService(VehiclePackageRepository vehiclePackageRepository,
                                 VehicleRepository vehicleRepository) {
        this.vehiclePackageRepository = vehiclePackageRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public List<VehiclePackage> getAll() {
        return vehiclePackageRepository.findAll();
    }

    public List<VehiclePackage> getActivatedPackages() {
        return vehiclePackageRepository.findByStatus("Activated");
    }

    /**
     * Get all visible packages (both activated and partially reserved)
     */
    public List<VehiclePackage> getVisiblePackages() {
        List<VehiclePackage> activated = vehiclePackageRepository.findByStatus("Activated");
        List<VehiclePackage> partiallyReserved = vehiclePackageRepository.findByStatus("Partially Reserved");
        activated.addAll(partiallyReserved);
        return activated;
    }

    public Optional<VehiclePackage> getById(Integer id) {
        return vehiclePackageRepository.findById(id);
    }

    @Transactional
    public VehiclePackage create(VehiclePackageRequest request) {
        VehiclePackage vp = new VehiclePackage();
        applyToEntity(vp, request);
        return vehiclePackageRepository.save(vp);
    }

    @Transactional
    public Optional<VehiclePackage> update(Integer id, VehiclePackageRequest request) {
        return vehiclePackageRepository.findById(id).map(existing -> {
            applyToEntity(existing, request);
            return vehiclePackageRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Integer id) {
        if (!vehiclePackageRepository.existsById(id)) return false;
        vehiclePackageRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Optional<VehiclePackage> setStatus(Integer id, String status) {
        return vehiclePackageRepository.findById(id).map(existing -> {
            existing.setStatus(status);
            return vehiclePackageRepository.save(existing);
        });
    }

    private void applyToEntity(VehiclePackage vp, VehiclePackageRequest request) {
        if (request.getPackageName() != null) vp.setPackageName(request.getPackageName());
        if (request.getPrice() != null) vp.setPrice(request.getPrice());
        if (request.getDuration() != null) vp.setDuration(request.getDuration());
        if (request.getImageUrl() != null) vp.setImageUrl(request.getImageUrl());
        if (request.getStatus() != null) vp.setStatus(request.getStatus());

        if (request.getVehicleIds() != null) {
            Iterable<Vehicle> vehicles = vehicleRepository.findAllById(request.getVehicleIds());
            Set<Vehicle> set = new HashSet<>();
            vehicles.forEach(set::add);
            vp.setVehicles(set);
        }
    }

    /**
     * Mark packages containing this vehicle as partially reserved (but still visible)
     * This is called when a vehicle is rented individually
     */
    @Transactional
    public void markPackagesAsPartiallyReserved(Integer vehicleId) {
        List<VehiclePackage> packages = vehiclePackageRepository.findActivatedPackagesContainingVehicle(vehicleId);
        for (VehiclePackage pkg : packages) {
            pkg.setStatus("Partially Reserved");
            vehiclePackageRepository.save(pkg);
        }
    }

    /**
     * Restore packages to activated status when vehicle is returned
     * This is called when a vehicle is returned from individual rental
     */
    @Transactional
    public void restorePackagesFromPartialReservation(Integer vehicleId) {
        List<VehiclePackage> packages = vehiclePackageRepository.findPackagesContainingVehicle(vehicleId);
        for (VehiclePackage pkg : packages) {
            // Only restore if it was partially reserved
            if ("Partially Reserved".equals(pkg.getStatus())) {
                // Check if all vehicles in the package are now available
                boolean allVehiclesAvailable = pkg.getVehicles().stream()
                    .allMatch(vehicle -> "Available".equals(vehicle.getStatus()));
                
                if (allVehiclesAvailable) {
                    pkg.setStatus("Activated");
                    vehiclePackageRepository.save(pkg);
                }
            }
        }
    }

    /**
     * Check if a package can be booked (not partially reserved)
     */
    public boolean isPackageAvailableForBooking(Integer packageId) {
        Optional<VehiclePackage> packageOpt = vehiclePackageRepository.findById(packageId);
        if (packageOpt.isPresent()) {
            VehiclePackage pkg = packageOpt.get();
            return "Activated".equals(pkg.getStatus());
        }
        return false;
    }

    /**
     * Get package booking availability message
     */
    public String getPackageAvailabilityMessage(Integer packageId) {
        Optional<VehiclePackage> packageOpt = vehiclePackageRepository.findById(packageId);
        if (packageOpt.isPresent()) {
            VehiclePackage pkg = packageOpt.get();
            switch (pkg.getStatus()) {
                case "Activated":
                    return "Available for booking";
                case "Partially Reserved":
                    return "Temporarily unavailable - One or more vehicles are currently rented individually";
                case "Deactivated":
                    return "Package is currently unavailable";
                default:
                    return "Status unknown";
            }
        }
        return "Package not found";
    }

    /**
     * Get packages containing a specific vehicle
     */
    public List<VehiclePackage> getPackagesContainingVehicle(Integer vehicleId) {
        return vehiclePackageRepository.findPackagesContainingVehicle(vehicleId);
    }

    /**
     * Get activated packages containing a specific vehicle
     */
    public List<VehiclePackage> getActivatedPackagesContainingVehicle(Integer vehicleId) {
        return vehiclePackageRepository.findActivatedPackagesContainingVehicle(vehicleId);
    }

    /**
     * Get comprehensive package analytics
     */
    public Map<String, Object> getPackageAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            List<VehiclePackage> allPackages = vehiclePackageRepository.findAll();
            System.out.println("DEBUG: Found " + allPackages.size() + " packages");
            
            // Print package details for debugging
            for (VehiclePackage pkg : allPackages) {
                System.out.println("DEBUG: Package - ID: " + pkg.getPackageId() + 
                                 ", Name: " + pkg.getPackageName() + 
                                 ", Price: " + pkg.getPrice() + 
                                 ", Status: " + pkg.getStatus());
            }
            
            // Package statistics
            Map<String, Object> packageStats = new HashMap<>();
            packageStats.put("totalPackages", allPackages.size());
            packageStats.put("activatedPackages", allPackages.stream().filter(p -> "Activated".equals(p.getStatus())).count());
            packageStats.put("deactivatedPackages", allPackages.stream().filter(p -> "Deactivated".equals(p.getStatus())).count());
            packageStats.put("partiallyReservedPackages", allPackages.stream().filter(p -> "Partially Reserved".equals(p.getStatus())).count());
            
            analytics.put("packageStats", packageStats);
            
            // Revenue analytics - Calculate meaningful potential revenue
            
            // Package bookings analytics
            List<Booking> packageBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getVehiclePackage() != null)
                .collect(Collectors.toList());
                
            Map<String, Object> bookingStats = new HashMap<>();
            bookingStats.put("totalPackageBookings", packageBookings.size());
            bookingStats.put("activePackageBookings", packageBookings.stream().filter(b -> "Rented".equals(b.getBookingStatus()) || "Booked".equals(b.getBookingStatus())).count());
            bookingStats.put("completedPackageBookings", packageBookings.stream().filter(b -> "Returned".equals(b.getBookingStatus())).count());
            
            // Calculate actual revenue from completed package bookings
            double actualRevenue = packageBookings.stream()
                .filter(booking -> "Returned".equals(booking.getBookingStatus()))
                .mapToDouble(booking -> booking.getTotalCost().doubleValue())
                .sum();
                
            // Calculate potential revenue from pending/active bookings 
            double pendingRevenue = packageBookings.stream()
                .filter(booking -> "Rented".equals(booking.getBookingStatus()) || "Booked".equals(booking.getBookingStatus()))
                .mapToDouble(booking -> booking.getTotalCost().doubleValue())
                .sum();
                
            // Total potential revenue is actual revenue + pending revenue
            double totalPotentialRevenue = actualRevenue + pendingRevenue;
            
            // If there are no bookings, calculate a theoretical monthly potential
            if (totalPotentialRevenue == 0) {
                totalPotentialRevenue = allPackages.stream()
                    .filter(p -> "Activated".equals(p.getStatus()))
                    .mapToDouble(pkg -> {
                        // Calculate estimated monthly revenue: package price * potential bookings per month
                        // Assume each package can be booked 2-3 times per month on average
                        double monthlyBookings = Math.max(1.0, 30.0 / pkg.getDuration());
                        return pkg.getPrice().doubleValue() * monthlyBookings;
                    })
                    .sum();
            }
            
            packageStats.put("totalPotentialRevenue", totalPotentialRevenue);
            packageStats.put("actualRevenue", actualRevenue);
            packageStats.put("pendingRevenue", pendingRevenue);
            
            analytics.put("bookingStats", bookingStats);
            
            // Package popularity (booking count per package)
            Map<Integer, Long> packagePopularity = packageBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getVehiclePackage().getPackageId(), Collectors.counting()));
            
            List<Map<String, Object>> popularityData = new ArrayList<>();
            for (VehiclePackage pkg : allPackages) {
                Map<String, Object> pkgData = new HashMap<>();
                pkgData.put("packageId", pkg.getPackageId());
                pkgData.put("packageName", pkg.getPackageName());
                pkgData.put("bookingCount", packagePopularity.getOrDefault(pkg.getPackageId(), 0L));
                pkgData.put("price", pkg.getPrice());
                pkgData.put("status", pkg.getStatus());
                popularityData.add(pkgData);
            }
            
            analytics.put("packagePopularity", popularityData);
            
            // Monthly booking trends (last 12 months)
            LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);
            List<Booking> recentBookings = packageBookings.stream()
                .filter(booking -> booking.getCreatedAt().isAfter(twelveMonthsAgo))
                .collect(Collectors.toList());
                
            Map<String, Long> monthlyBookings = new HashMap<>();
            for (int i = 11; i >= 0; i--) {
                LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
                
                long count = recentBookings.stream()
                    .filter(booking -> booking.getCreatedAt().isAfter(monthStart) && booking.getCreatedAt().isBefore(monthEnd))
                    .count();
                    
                String monthKey = monthStart.getMonth().toString() + " " + monthStart.getYear();
                monthlyBookings.put(monthKey, count);
            }
            
            analytics.put("monthlyTrends", monthlyBookings);
            
            // Revenue by package
            Map<String, Double> packageRevenue = new HashMap<>();
            for (VehiclePackage pkg : allPackages) {
                double revenue = packageBookings.stream()
                    .filter(booking -> pkg.getPackageId().equals(booking.getVehiclePackage().getPackageId()))
                    .filter(booking -> "Returned".equals(booking.getBookingStatus()) || "Rented".equals(booking.getBookingStatus()))
                    .mapToDouble(booking -> booking.getTotalCost().doubleValue())
                    .sum();
                packageRevenue.put(pkg.getPackageName(), revenue);
            }
            
            analytics.put("packageRevenue", packageRevenue);
            
        } catch (Exception e) {
            analytics.put("error", "Failed to generate analytics: " + e.getMessage());
        }
        
        return analytics;
    }

    /**
     * Get analytics for a specific package
     */
    public Map<String, Object> getPackageAnalyticsById(Integer packageId) {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            Optional<VehiclePackage> packageOpt = vehiclePackageRepository.findById(packageId);
            if (packageOpt.isEmpty()) {
                analytics.put("error", "Package not found");
                return analytics;
            }
            
            VehiclePackage pkg = packageOpt.get();
            analytics.put("packageInfo", Map.of(
                "id", pkg.getPackageId(),
                "name", pkg.getPackageName(),
                "price", pkg.getPrice(),
                "duration", pkg.getDuration(),
                "status", pkg.getStatus(),
                "vehicleCount", pkg.getVehicles().size()
            ));
            
            // Get bookings for this package
            List<Booking> packageBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getVehiclePackage() != null && packageId.equals(booking.getVehiclePackage().getPackageId()))
                .collect(Collectors.toList());
                
            // Booking statistics
            Map<String, Object> bookingStats = new HashMap<>();
            bookingStats.put("totalBookings", packageBookings.size());
            bookingStats.put("activeBookings", packageBookings.stream().filter(b -> "Rented".equals(b.getBookingStatus()) || "Booked".equals(b.getBookingStatus())).count());
            bookingStats.put("completedBookings", packageBookings.stream().filter(b -> "Returned".equals(b.getBookingStatus())).count());
            bookingStats.put("cancelledBookings", packageBookings.stream().filter(b -> "Cancelled".equals(b.getBookingStatus())).count());
            
            double totalRevenue = packageBookings.stream()
                .filter(booking -> "Returned".equals(booking.getBookingStatus()) || "Rented".equals(booking.getBookingStatus()))
                .mapToDouble(booking -> booking.getTotalCost().doubleValue())
                .sum();
            bookingStats.put("totalRevenue", totalRevenue);
            
            analytics.put("bookingStats", bookingStats);
            
            // Monthly booking trends for this package
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            List<Booking> recentBookings = packageBookings.stream()
                .filter(booking -> booking.getCreatedAt().isAfter(sixMonthsAgo))
                .collect(Collectors.toList());
                
            Map<String, Long> monthlyBookings = new HashMap<>();
            for (int i = 5; i >= 0; i--) {
                LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
                
                long count = recentBookings.stream()
                    .filter(booking -> booking.getCreatedAt().isAfter(monthStart) && booking.getCreatedAt().isBefore(monthEnd))
                    .count();
                    
                String monthKey = monthStart.getMonth().toString() + " " + monthStart.getYear();
                monthlyBookings.put(monthKey, count);
            }
            
            analytics.put("monthlyTrends", monthlyBookings);
            
        } catch (Exception e) {
            analytics.put("error", "Failed to generate package analytics: " + e.getMessage());
        }
        
        return analytics;
    }
}

