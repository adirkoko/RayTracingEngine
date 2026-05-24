package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link primitives.Material}.
 */
class MaterialTest {

    /**
     * Test default values for advanced global effect parameters.
     */
    @Test
    void testAdvancedGlobalEffectDefaults() {
        Material material = new Material();

        assertEquals(0, material.reflectionBlur, "Default reflection blur should keep perfect reflection");
        assertEquals(0, material.transparencyBlur, "Default transparency blur should keep perfect transparency");
        assertEquals(1, material.globalSamples, "Default global samples should keep single-ray behavior");
    }

    /**
     * Test setters for advanced global effect parameters.
     */
    @Test
    void testAdvancedGlobalEffectSetters() {
        Material material = new Material()
                .setReflectionBlur(0.5)
                .setTransparencyBlur(0.25)
                .setGlobalSamples(9);

        assertEquals(0.5, material.reflectionBlur, "Reflection blur should be updated");
        assertEquals(0.25, material.transparencyBlur, "Transparency blur should be updated");
        assertEquals(9, material.globalSamples, "Global samples should be updated");
    }

    /**
     * Test validation for advanced global effect parameters.
     */
    @Test
    void testAdvancedGlobalEffectValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new Material().setReflectionBlur(-1),
                "Reflection blur should reject negative values");

        assertThrows(IllegalArgumentException.class,
                () -> new Material().setTransparencyBlur(-1),
                "Transparency blur should reject negative values");

        assertThrows(IllegalArgumentException.class,
                () -> new Material().setGlobalSamples(0),
                "Global samples should reject zero");

        assertThrows(IllegalArgumentException.class,
                () -> new Material().setGlobalSamples(-1),
                "Global samples should reject negative values");

        assertThrows(IllegalArgumentException.class,
                () -> new Material().setGlobalSamples(Material.MAX_GLOBAL_SAMPLES + 1),
                "Global samples should reject values above the configured cap");
    }
}
