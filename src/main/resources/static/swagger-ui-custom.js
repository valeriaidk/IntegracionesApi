// Custom Swagger UI script to update example with real response
window.addEventListener('load', function () {
  // Wait for Swagger UI to load
  setTimeout(function() {
    if (window.ui) {
      // Intercept responses
      const originalExecute = window.ui.fn.execute;
      window.ui.fn.execute = function (req) {
        return originalExecute.call(this, req).then(function (response) {
          // After execution, update the example in the DOM
          setTimeout(function() {
            const responseBody = response.data;
            if (responseBody && typeof responseBody === 'object') {
              // Find the example value element and update it
              const exampleElements = document.querySelectorAll('.response .example pre');
              if (exampleElements.length > 0) {
                exampleElements[0].textContent = JSON.stringify(responseBody, null, 2);
              }
            }
          }, 100);
          return response;
        });
      };
    }
  }, 2000);
});