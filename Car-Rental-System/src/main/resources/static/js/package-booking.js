/**
 * Package Booking Functionality
 */

// Calculate package price when dates are selected
function calculatePackageBookingDetails() {
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
  // Extract numeric value, handling Indian currency format (e.g., "Rs. 25000")
  const priceStr = packagePriceText.replace(/[^\d]/g, '');
  const packagePrice = parseInt(priceStr, 10);

  // Update cost calculation display
  document.getElementById("package-price-display").textContent = `Rs. ${packagePrice.toFixed(2)}`;
  document.getElementById("package-days-display").textContent = `${duration} day${duration !== 1 ? 's' : ''}`;

  // Calculate and display total cost
  // For packages, we just use the fixed price (not multiplied by days)
  const totalCost = packagePrice;
  document.getElementById("package-total-cost").textContent = `Rs. ${totalCost.toFixed(2)}`;
}
