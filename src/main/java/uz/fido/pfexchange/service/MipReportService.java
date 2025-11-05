package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.mip.MipReportRequestDto;
import uz.fido.pfexchange.dto.mip.MipReportResponseDto;

public interface MipReportService {
    MipReportResponseDto pensionInfo(MipReportRequestDto requestDto);
}
