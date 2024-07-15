package com.skymilk.androidpdfwriter

import android.content.Context
import android.widget.Toast
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Link
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.Leading
import com.itextpdf.layout.property.Property
import com.itextpdf.layout.property.TextAlignment
import java.io.File

class PdfDocumentGenerator(
    private val context: Context
) {

    fun generateInvoicePdf(invoice: Invoice) {
        val outputFile = File(context.filesDir, "invoice.pdf")
        val pdfWriter = PdfWriter(outputFile)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4).apply {// 페이지 사이즈
            setMargins(50f, 13f, 13f, 13f) // 여백 설정
            setProperty(
                Property.LEADING,
                Leading(Leading.MULTIPLIED, 1f)
            )
        }.setWordSpacing(0f) //문자 간격 설정

        //페이지 추가
        val page = pdfDocument.addNewPage()

        //거래 명세서 번호
        val invoiceNumber = Paragraph("Invoice #${invoice.number}")
            .setBold()
            .setFontSize(32f)

        // 결제 날짜
        val invoiceDate = createLightTextParagraph(invoice.date)

        // 결제 링크
        val payLink = Link("Pay ${invoice.price}", PdfAction.createURI(invoice.link))
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontColor(DeviceRgb.WHITE)

        // 결제 버튼
        val payParagraph = Paragraph().apply {
            add(payLink)
            setBackgroundColor(DeviceRgb(0, 92, 230))
            setPadding(12f)
            setWidth(100f)
            setHorizontalAlignment(HorizontalAlignment.RIGHT)
            setTextAlignment(TextAlignment.CENTER)
        }

        //헤더 영역
        val headerSection = Table(2, true).apply {
            setMarginLeft(15f)
            setMarginRight(15f)
            setWidth(page.pageSize.width - 30 - 26f)

            addCell(createBorderlessCell(invoiceNumber))
            addCell(createBorderlessCell(payParagraph))
            addCell(createBorderlessCell(invoiceDate))
        }

        //헤더 작성
        document.add(headerSection)
        document.add(createLineSeparator())


        //저장
        document.close()
        Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show()
    }

    //텍스트 단락 생성
    private fun createLightTextParagraph(text: String): Paragraph {
        val lightTextStyle = Style().apply {
            setFontSize(12f)
            setFontColor(DeviceRgb(166, 166, 166))
        }

        return Paragraph(text).addStyle(lightTextStyle)
    }

    //테두리 없는 셀 생성
    private fun createBorderlessCell(paragraph: Paragraph): Cell {
        return Cell().add(paragraph).setBorder(null)
    }

    //구분선 설정
    private fun createLineSeparator(): LineSeparator {
        return LineSeparator(
            SolidLine().apply {
                color = DeviceRgb(204, 204, 204)
            }
        ).setMarginTop(20f)
    }
}