// Dashboard vehicles functionality

// Global vehicles array to hold all vehicles
let allVehicles = [];

// Load available vehicles function implementation
function loadAvailableVehicles() {
  const loadingElement = document.getElementById("loading-vehicles");
  const vehiclesContainer = document.getElementById("vehicles-container");
  const vehiclesGrid = document.getElementById("vehicles-grid");

  // Show loading state
  loadingElement.style.display = "block";
  vehiclesContainer.style.display = "none";

  const token = localStorage.getItem("token");
  const headers = {
    'Accept': 'application/json'
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  fetch('/api/vehicles/available', {
    method: 'GET',
    headers: headers
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  })
  .then(vehicles => {
    allVehicles = vehicles;
    displayVehicles(vehicles);

    // Hide loading and show vehicles container
    loadingElement.style.display = "none";
    vehiclesContainer.style.display = "block";
  })
  .catch(error => {
    console.error('Error loading vehicles:', error);
    loadingElement.innerHTML = 'Failed to load vehicles. Please try again later.';
    loadingElement.style.color = 'var(--danger-color)';
  });
}

// Display vehicles in the grid
function displayVehicles(vehicles) {
  const vehiclesGrid = document.getElementById("vehicles-grid");

  if (!vehicles || vehicles.length === 0) {
    vehiclesGrid.innerHTML = '<p style="text-align: center; color: var(--light-text); grid-column: 1 / -1;">No vehicles available at the moment.</p>';
    return;
  }

  vehiclesGrid.innerHTML = '';

  vehicles.forEach(vehicle => {
    const vehicleCard = document.createElement('div');
    vehicleCard.className = 'vehicle-card';

    // Handle vehicle image
    const imageUrl = vehicle.imageUrl || 'https://via.placeholder.com/200x120?text=No+Image';

    // Calculate display price (with discount if applicable)
    let priceDisplay = `Rs. ${vehicle.rentalRatePerDay}/day`;
    if (vehicle.has_discount && vehicle.discountedRatePerDay) {
      priceDisplay = `
        <span style="text-decoration: line-through; color: var(--light-text);">Rs. ${vehicle.rentalRatePerDay}/day</span><br>
        <span style="color: var(--danger-color); font-weight: bold;">Rs. ${vehicle.discountedRatePerDay}/day</span>
      `;
    }

    vehicleCard.innerHTML = `
      <img src="${imageUrl}" alt="${vehicle.make} ${vehicle.model}" class="vehicle-image" onerror="this.src='https://via.placeholder.com/200x120?text=No+Image'">
      <div class="vehicle-info">
        <div class="vehicle-make-model">${vehicle.make} ${vehicle.model}</div>
        <div class="vehicle-year">${vehicle.year}</div>
        <div class="vehicle-price">${priceDisplay}</div>
        <div class="vehicle-status" style="color: ${vehicle.status === 'AVAILABLE' ? 'var(--accent-color)' : 'var(--light-text)'}">
          ${vehicle.status}
        </div>
        <button class="view-details-btn" onclick="viewVehicleDetails(${vehicle.vehicleId})">
          View Details
        </button>
      </div>
    `;

    vehiclesGrid.appendChild(vehicleCard);
  });
}

// Filter vehicles based on search input
function filterVehicles() {
  const searchTerm = document.getElementById("vehicle-search").value.toLowerCase();

  if (!searchTerm) {
    displayVehicles(allVehicles);
    return;
  }

  const filteredVehicles = allVehicles.filter(vehicle => {
    return (
      vehicle.make.toLowerCase().includes(searchTerm) ||
      vehicle.model.toLowerCase().includes(searchTerm) ||
      vehicle.year.toString().includes(searchTerm) ||
      (vehicle.registrationNumber && vehicle.registrationNumber.toLowerCase().includes(searchTerm))
    );
  });

  displayVehicles(filteredVehicles);
}
