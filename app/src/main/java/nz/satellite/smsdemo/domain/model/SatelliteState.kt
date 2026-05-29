package nz.satellite.smsdemo.domain.model

/** Whether the device is currently reaching the network via satellite. */
enum class SatelliteState {
    /** On a non-terrestrial (satellite) network. */
    Satellite,

    /** On a normal terrestrial cellular network. */
    Terrestrial,

    /** Unknown — permission missing, pre-API-35, or no service. */
    Unknown,
}
