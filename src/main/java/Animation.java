public interface Animation {
    // Step the animation by a frame
    public void animate();

    // Delay the animatoin for x frames
    public void delay(int frames);

    // Toggle the playing of the animation
    public void toggle();

    // Return whether or not the animation is playing
    public boolean isAnimating();

}
