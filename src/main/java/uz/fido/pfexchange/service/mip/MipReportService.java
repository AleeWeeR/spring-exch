package uz.fido.pfexchange.service.mip;

import uz.fido.pfexchange.dto.mip.report.MipReportRequestDto;
import uz.fido.pfexchange.dto.mip.report.MipReportResponseDto;

public interface MipReportService {
    MipReportResponseDto pensionInfo(MipReportRequestDto requestDto);
}
