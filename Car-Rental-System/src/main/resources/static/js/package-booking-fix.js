/**
 * Fixed package price calculation for Car Rental System
 * This script fixes the issue with the price calculation in the package booking modal
 */

document.addEventListener('DOMContentLoaded', function() {
  // Override the calculatePackageBookingDetails function with our fixed version
  window.calculatePackageBookingDetails = function() {
    const startDateInput = document.getElementById("package-start-date");
    const endDateInput = document.getElementById("package-end-date");

    // Only calculate if both dates are selected
    if (!startDateInput.value || !endDateInput.value) return;

    const startDate = new Date(startDateInput.value);
    const endDate = new Date(endDateInput.value);

    // Set minimum end date based on start date
    const nextDay = new Date(startDate);
    nextDay.setDate(nextDay.getDate() + 1);
    endDateInput.min = nextDay.toISOString().split('T')[0];

    // Reset end date if it's before start date
    if (endDate <= startDate) {
      endDateInput.value = nextDay.toISOString().split('T')[0];
      endDate.setTime(nextDay.getTime());
    }

    // Calculate duration in days
    const duration = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));
    document.getElementById("package-rental-duration").textContent = `${duration} day${duration !== 1 ? 's' : ''}`;

    // Get package price from the booking modal display
    const packagePriceText = document.getElementById("package-booking-price").textContent;

    // Extract the numeric price value correctly
    // This handles formats like "Rs. 25000" correctly
    let packagePrice = 0;
    const priceMatch = packagePriceText.match(/Rs\.\s*(\d+)/);
    if (priceMatch) {
      packagePrice = parseInt(priceMatch[1], 10);
    }

    // Update cost calculation display
    document.getElementById("package-price-display").textContent = `Rs. ${packagePrice.toFixed(2)}`;
    document.getElementById("package-days-display").textContent = `${duration} day${duration !== 1 ? 's' : ''}`;

    // Calculate and display total cost - packages are fixed price, not per day
    const totalCost = packagePrice;
    document.getElementById("package-total-cost").textContent = `Rs. ${totalCost.toFixed(2)}`;
  };

  // Re-attach event listeners for date changes
  const startDateInput = document.getElementById("package-start-date");
  const endDateInput = document.getElementById("package-end-date");
  if (startDateInput && endDateInput) {
    startDateInput.addEventListener("change", calculatePackageBookingDetails);
    endDateInput.addEventListener("change", calculatePackageBookingDetails);
  }
});
