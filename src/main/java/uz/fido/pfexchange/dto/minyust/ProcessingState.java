package uz.fido.pfexchange.dto.minyust;

import uz.fido.pfexchange.enums.MinyustState;

public record ProcessingState(MinyustState state, String threadName) {}
