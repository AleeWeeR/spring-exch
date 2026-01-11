package uz.fido.pfexchange.utils.validation;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

@GroupSequence({FirstCheck.class, SecondCheck.class, Default.class, ThirdCheck.class})
public interface OrderedChecks {}
