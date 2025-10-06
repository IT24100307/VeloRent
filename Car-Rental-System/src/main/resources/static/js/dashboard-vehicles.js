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
    console.log('Vehicles loaded successfully:', vehicles.length);
    allVehicles = vehicles;
    displayVehicles(vehicles);

    // Populate filter dropdowns with a slight delay to ensure DOM is ready
    setTimeout(() => {
      if (window.populateFilterDropdowns) {
        console.log('Populating filter dropdowns');
        window.populateFilterDropdowns();
      } else {
        console.warn('populateFilterDropdowns function not found');
      }
    }, 100);

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
  console.log('displayVehicles called with', vehicles?.length || 0, 'vehicles');
  const vehiclesGrid = document.getElementById("vehicles-grid");

  if (!vehiclesGrid) {
    console.error('vehicles-grid element not found');
    return;
  }

  if (!vehicles || vehicles.length === 0) {
    vehiclesGrid.innerHTML = '<div style="text-align: center; color: var(--text-luxury-muted); grid-column: 1 / -1; padding: 40px; font-size: 1.2rem;"><i class="fas fa-car" style="font-size: 3rem; margin-bottom: 20px; opacity: 0.3;"></i><br>No vehicles match your criteria.<br><small style="opacity: 0.7;">Try adjusting your filters or search terms.</small></div>';
    return;
  }

  vehiclesGrid.innerHTML = '';
  console.log('Rendering', vehicles.length, 'vehicles');

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
      <div class="vehicle-image-container" style="position: relative; overflow: hidden; border-radius: var(--radius-luxury-medium); margin-bottom: 20px;">
        <img src="${imageUrl}" alt="${vehicle.make} ${vehicle.model}" class="vehicle-image" onerror="this.src='https://via.placeholder.com/300x220/1a1a1a/d4af37?text=No+Image+Available'">
        <div class="vehicle-status-badge" style="position: absolute; top: 15px; right: 15px; padding: 6px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; ${
          vehicle.status === 'AVAILABLE' 
            ? 'background: linear-gradient(135deg, #27ae60, #2ecc71); color: white; box-shadow: 0 4px 15px rgba(39, 174, 96, 0.3);' 
            : 'background: linear-gradient(135deg, #95a5a6, #7f8c8d); color: white; box-shadow: 0 4px 15px rgba(149, 165, 166, 0.3);'
        }">
          ${vehicle.status}
        </div>
      </div>
      <div class="vehicle-info" style="flex-grow: 1; display: flex; flex-direction: column; justify-content: space-between;">
        <div class="vehicle-details">
          <div class="vehicle-make-model" style="font-size: 1.4rem; font-weight: 700; color: var(--luxury-gold); margin-bottom: 8px; font-family: var(--font-luxury-primary);">
            ${vehicle.make} ${vehicle.model}
          </div>
          <div class="vehicle-year" style="color: var(--text-luxury-muted); font-size: 1rem; margin-bottom: 15px; font-weight: 500;">
            <i class="fas fa-calendar-alt" style="margin-right: 8px; color: var(--luxury-gold);"></i>Year: ${vehicle.year}
          </div>
          <div class="vehicle-price" style="font-size: 1.5rem; font-weight: 700; margin: 15px 0; color: var(--luxury-gold); font-family: var(--font-luxury-primary);">
            ${priceDisplay}
          </div>
        </div>
        <button class="view-details-btn" onclick="viewVehicleDetails(${vehicle.vehicleId})" type="button" 
          style="width: 100%; padding: 12px 20px; background: linear-gradient(135deg, var(--luxury-gold), #f1c40f); 
          color: var(--luxury-black); border: none; border-radius: var(--radius-luxury-small); 
          font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; 
          transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1); cursor: pointer; 
          box-shadow: 0 4px 15px rgba(212, 175, 55, 0.3); margin-top: auto;">
          <i class="fas fa-eye" style="margin-right: 8px;"></i> View Details
        </button>
      </div>
    `;

    vehiclesGrid.appendChild(vehicleCard);
  });
}

// Filter vehicles based on search input (legacy support)
function filterVehicles() {
  // Call the new combined function if it exists, otherwise use basic filtering
  if (window.filterAndSortVehicles) {
    window.filterAndSortVehicles();
    return;
  }
  
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
