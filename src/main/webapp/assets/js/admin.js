document.addEventListener("DOMContentLoaded", () => {
  const wrapper = document.getElementById("wrapper");
  const menuToggle = document.getElementById("menu-toggle");
  menuToggle.addEventListener("click", () => {
    wrapper.classList.toggle("toggled");
  });
});
	