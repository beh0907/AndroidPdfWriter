package com.skymilk.androidpdfwriter

import android.annotation.SuppressLint
import android.content.Context
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Link
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.Leading
import com.itextpdf.layout.property.Property
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.text.DecimalFormat


@SuppressLint("ResourceType")
class PdfDocumentGenerator(
    private val context: Context
) {

    //한글 처리를 위한 지정 폰트
    //배민 한나 프로
    private var fontHannaPro: PdfFont

    //원화 콤마 표기
    private var commaDecimal = DecimalFormat("#,###")

    init {
        // 폰트 파일 로드
        val fontStream: InputStream = context.resources.openRawResource(R.font.bm_hanna_pro)
        fontHannaPro = PdfFontFactory.createFont(
            fontStream.readBytes(),
            PdfEncodings.IDENTITY_H,
            PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
        )
    }

    suspend fun generateInvoicePdf(invoice: Invoice, outputStream: OutputStream) {
        //이미지를 불러오기 위한 네트워크 통신을 위해 IO 디스패처
        withContext(Dispatchers.IO) {

//            val outputFile = File(context.filesDir, "invoice.pdf")
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize(650f, 700f)).apply {// 페이지 사이즈
                setMargins(50f, 13f, 13f, 13f) // 여백 설정
                setProperty(
                    Property.LEADING,
                    Leading(Leading.MULTIPLIED, 1f)
                )
            }.setWordSpacing(0f) //문자 간격 설정

            //페이지 추가
            val page = pdfDocument.addNewPage()

            //거래 명세서 번호
            val invoiceNumber = Paragraph("Invoice #${invoice.number}").apply {
                setBold()
                setFontSize(32f)
                setFont(fontHannaPro)
            }

            // 결제 날짜
            val invoiceDate = createLightTextParagraph(invoice.date)

            // 결제 링크
            val payLink = Link("결제 하기", PdfAction.createURI(invoice.link)).apply {
                setTextAlignment(TextAlignment.RIGHT)
                setFontColor(DeviceRgb.WHITE)
                setFont(fontHannaPro)
            }

            // 결제 버튼
            val payParagraph = Paragraph().apply {
                add(payLink)
                setBackgroundColor(DeviceRgb(0, 92, 230))
                setPadding(12f)
                setWidth(100f)
                setHorizontalAlignment(HorizontalAlignment.RIGHT)
                setTextAlignment(TextAlignment.CENTER)
            }

            //헤더 영역 테이블
            val headerSection = Table(2, false).apply {
                //레이아웃 설정
                setMarginLeft(15f)
                setMarginRight(15f)
                setWidth(page.pageSize.width - 30 - 26f)

                //배치
                addCell(createBorderlessCell(invoiceNumber))
                addCell(createBorderlessCell(payParagraph))
                addCell(createBorderlessCell(invoiceDate))
            }
            //헤더 및 구분선 추가
            document.add(headerSection)
            document.add(createLineSeparator())


            //구매/판매자 정보 영역
            val from = createLightTextParagraph("From").setTextAlignment(TextAlignment.LEFT)
            val to = createLightTextParagraph("To").setTextAlignment(TextAlignment.RIGHT)
            val fromName =
                createBoldTextParagraph(invoice.from.name).setTextAlignment(TextAlignment.LEFT)
            val toName =
                createBoldTextParagraph(invoice.to.name).setTextAlignment(TextAlignment.RIGHT)
            val fromAddress =
                createLightTextParagraph(invoice.from.address).setTextAlignment(TextAlignment.LEFT)
            val toAddress =
                createLightTextParagraph(invoice.to.address).setTextAlignment(TextAlignment.RIGHT)

            //구매/판매자 영역 테이블
            val peopleTable = Table(2, true).apply {
                //레이아웃 설정
                setMarginLeft(15f)
                setMarginRight(15f)
                setMarginTop(50f)

                //배치
                addCell(createBorderlessCell(from))
                addCell(createBorderlessCell(to))
                addCell(createBorderlessCell(fromName))
                addCell(createBorderlessCell(toName))
                addCell(createBorderlessCell(fromAddress))
                addCell(createBorderlessCell(toAddress))
            }
            //구매/판매자 정보 추가
            document.add(peopleTable)


            //상품 목록 영역 타이틀
            val descriptionTitle =
                createBoldTextParagraph("상품명").setTextAlignment(TextAlignment.CENTER)
            val rateTitle = createBoldTextParagraph("단가").setTextAlignment(TextAlignment.CENTER)
            val qtyTitle = createBoldTextParagraph("수량").setTextAlignment(TextAlignment.CENTER)
            val subTotalTitle = createBoldTextParagraph("소계").setTextAlignment(TextAlignment.CENTER)

            //구매/판매자 영역 테이블
            val productsTable = Table(4, true).apply {
                //레이아웃 설정
                setMarginLeft(15f)
                setMarginRight(15f)
                setMarginTop(50f)

                //테이블 헤더 타이틀 추가
                addCell(createProductTableCell(descriptionTitle))
                addCell(createProductTableCell(rateTitle))
                addCell(createProductTableCell(qtyTitle))
                addCell(createProductTableCell(subTotalTitle))
            }

            val lightBlack = DeviceRgb(64, 64, 64)
            //상품 목록 추가
            invoice.products.forEach { product: Product ->
                val description = createBoldTextParagraph(
                    product.description,
                    lightBlack
                ).setTextAlignment(TextAlignment.CENTER)

                val rate = createBoldTextParagraph(
                    "${commaDecimal.format(product.rate)}원",
                    lightBlack
                ).setTextAlignment(TextAlignment.CENTER)

                val qty = createBoldTextParagraph(
                    commaDecimal.format(product.quantity),
                    lightBlack
                ).setTextAlignment(TextAlignment.CENTER)

                val subTotal = createBoldTextParagraph(
                    "${commaDecimal.format(product.rate * product.quantity)}원",
                    lightBlack
                ).setTextAlignment(TextAlignment.CENTER)

                //상품별 정보 셀 추가
                productsTable.addCell(createProductTableCell(description))
                productsTable.addCell(createProductTableCell(rate))
                productsTable.addCell(createProductTableCell(qty))
                productsTable.addCell(createProductTableCell(subTotal))
            }

            //총 합계 비용 영역
            val grandTotal = createLightTextParagraph("총 합계").apply {
                setFontColor(DeviceRgb(166, 166, 166))
                setFontSize(16f)
                setTextAlignment(TextAlignment.RIGHT)
            }
            val grandTotalCell = Cell(1, 4).apply {
                setPaddingTop(20f)
                setPaddingBottom(20f)

                add(grandTotal)
                setBorder(null)

                setBorderBottom(SolidBorder(DeviceRgb(204, 204, 204), 2f))
            }
            //합계 타이틀 셀 추가
            productsTable.addCell(grandTotalCell)

            //사인 이미지 가져오기
            invoice.signatureUrl?.let {
                val imageData = ImageDataFactory.create(URL(it))
                val image = Image(imageData).apply {
                    setWidth(50f)
                    setHeight(50f)

                    setTextAlignment(TextAlignment.LEFT)
                }

                //이미지 셀 추가
                productsTable.addCell(Cell(1, 2).setPaddingTop(10f).setBorder(null).add(image))
            }

            //총 합계 가격 정보 추가
            val totalPrice =
                createBoldTextParagraph("${getTotalPrice(invoice.products)}원").setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
            val totalPriceCell =
                Cell(1, if (invoice.signatureUrl == null) 4 else 2).apply {
                    setPaddingTop(10f)
                    setBorder(null)
                    setVerticalAlignment(VerticalAlignment.MIDDLE)
                    add(totalPrice)
                }
            productsTable.addCell(totalPriceCell)

            //상품 목록 테이블 추가
            document.add(productsTable)

            //저장
            document.close()
        }
    }

    //텍스트 단락 생성
    private fun createLightTextParagraph(text: String): Paragraph {
        val lightTextStyle = Style().apply {
            setFontSize(12f)
            setFontColor(DeviceRgb(166, 166, 166)) // 회색
            setFont(fontHannaPro)
        }
        return Paragraph(text).addStyle(lightTextStyle)
    }

    //굵은 텍스트 단락 생성
    private fun createBoldTextParagraph(text: String, color: Color = DeviceRgb.BLACK): Paragraph {
        val boldTextStyle = Style().apply {
            setFontSize(16f)
            setFontColor(color)
            setVerticalAlignment(VerticalAlignment.MIDDLE)
            setBold()
            setFont(fontHannaPro)
        }
        return Paragraph(text).addStyle(boldTextStyle)
    }

    //테두리 없는 셀 생성
    private fun createBorderlessCell(paragraph: Paragraph): Cell {
        return Cell().add(paragraph).setBorder(null)
    }

    //상품 정보 셀 생성
    private fun createProductTableCell(paragraph: Paragraph): Cell {
        return Cell().add(paragraph).apply {
            setPaddingBottom(20f)
            setPaddingTop(20f)
            setBorder(null)

            //하단 테두리를 추가해 구분선처럼 활용
            setBorderBottom(SolidBorder(DeviceRgb(166, 166, 166), 1f))
        }
    }

    //구분선 설정
    private fun createLineSeparator(): LineSeparator {
        return LineSeparator(
            SolidLine().apply {
                color = DeviceRgb(204, 204, 204)
            }
        ).setMarginTop(20f)
    }

    //총 합계 가격 구하기
    private fun getTotalPrice(products: List<Product>): String {
        var totalPrice = 0

        products.forEach { totalPrice += it.rate * it.quantity }

        return commaDecimal.format(totalPrice)
    }
}