package com.skymilk.androidpdfwriter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Link
import com.itextpdf.layout.element.List
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.ListNumberingType
import com.itextpdf.layout.property.TextAlignment
import com.skymilk.androidpdfwriter.ui.theme.AndroidPdfWriterTheme
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val pdfDocumentGenerator = PdfDocumentGenerator(this)

        setContent {
            AndroidPdfWriterTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val createDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                    onResult = {
                        //지정 경로 정보의 유효성을 체크한다
                        it.data?.data?.let {
                            context.contentResolver.openOutputStream(it)?.let {

                                scope.launch {
                                    pdfDocumentGenerator.generateInvoicePdf(
                                        outputStream = it,
                                        invoice = Invoice(
                                            number = 7877859L,
                                            price = 885.0f,
                                            link = "https://www.google.com",
                                            date = "2024-07-17 수요일",
                                            from = PersonInfo(
                                                name = "박인협",
                                                address = "서울시 강동구 강일동 599-10"
                                            ),
                                            to = PersonInfo(
                                                name = "개발스토어",
                                                address = "서울시 강동구 강일동 599-10"
                                            ),
                                            listOf(
                                                Product(
                                                    description = "대시보드 디자인",
                                                    rate = 900000,
                                                    quantity = 5
                                                ),
                                                Product(
                                                    description = "로고 디자인",
                                                    rate = 180000,
                                                    quantity = 2
                                                ),
                                                Product(
                                                    description = "썸네일 디자인",
                                                    rate = 250000,
                                                    quantity = 1
                                                ),
//                                                Product(
//                                                    description = "아이콘 디자인",
//                                                    rate = 60000,
//                                                    quantity = 10
//                                                ),
//                                                Product(
//                                                    description = "서버",
//                                                    rate = 4000000,
//                                                    quantity = 2
//                                                ),
//                                                Product(
//                                                    description = "서버 관리",
//                                                    rate = 1300000,
//                                                    quantity = 1
//                                                ),
//                                                Product(
//                                                    description = "서비스 개발",
//                                                    rate = 3000000,
//                                                    quantity = 1
//                                                ),
//                                                Product(
//                                                    description = "서비스 운영",
//                                                    rate = 550000,
//                                                    quantity = 1
//                                                ),
                                            ),
                                            signatureUrl = "https://w7.pngwing.com/pngs/962/173/png-transparent-signature-signature-miscellaneous-angle-material.png"
                                        )
                                    )
                                }
                            }
                        }
                    }
                )


                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {


                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Button(onClick = {
                            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                type = "application/pdf" //intent 타입 지정
                                putExtra(Intent.EXTRA_TITLE, "invoice.pdf") // 기본 파일명
                            }.also {
                                createDocumentLauncher.launch(it)
                            }
                        }) {
                            Text(text = "저장하기")
                        }
                    }
                }
            }
        }
    }
}


//PDF 문서 생성 기능 테스트
private fun createPdfLearn(context: Context) {
    val outputFile = File(context.filesDir, "createTest.pdf")
    val pdfWriter = PdfWriter(outputFile)
    val pdfDocument = PdfDocument(pdfWriter)
    val document = Document(pdfDocument, PageSize.A3) // 페이지는 A3 사이즈로 지정

    //PDF 페이지 추가
    pdfDocument.addNewPage()
    //페이지 여백 설정
    document.setMargins(0f, 0f, 0f, 0f)
    val fontHannaPro = ResourcesCompat.getFont(context, R.font.bm_hanna_pro)
//    ResourcesCompat.getFont(context, R.font.BMHANNAProOTF

//    // 폰트 파일을 assets에서 불러오기
//    val fontStream: InputStream = context.assets.open("fonts/bm_hanna_pro.otf");
//    PdfFontFactory.createFont(fontStream.readBytes());

    //추가
    document.add(createText())
    document.add(createLineSeparator())
    document.add(createList())
    document.add(createLineSeparator())
    document.add(createLink())
    document.add(createLineSeparator())
    document.add(createTable(3))

    //저장
    document.close()
    Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show()
}

//단락 텍스트 설정
fun createText(): Paragraph {
    //페이지 폰트 설정
    val fontTimesBold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD)

    return Paragraph("Hello World!")
        .setTextAlignment(TextAlignment.CENTER)
        .setFontSize(22f)
        .setFontColor(DeviceRgb.BLUE)
        .setFont(fontTimesBold)
}

//목록 설정
fun createList(): List {
    //페이지 폰트 설정
    val fontTimesRoman = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN)

    val list = List()
    list.setListSymbol(ListNumberingType.DECIMAL)
    list.addStyle(
        Style()
            .setFontSize(16f)
            .setFont(fontTimesRoman)
    )
    //목록 아이템 추가
    for (i in 1..10) list.add("Item $i")

    return list
}

//하이퍼링크 설정
fun createLink(): Paragraph {
    //하이퍼 링크
    val link = Link(
        "Naver", PdfAction.createURI("https://www.naver.com")
    )
        .setFontSize(30f)
        .setBold()
        .setUnderline()

    return Paragraph().add(link).setTextAlignment(TextAlignment.CENTER)
}

//구분선 설정
fun createLineSeparator(): LineSeparator {
    return LineSeparator(SolidLine())
}

//테이블 설정
fun createTable(cols: Int): Table {
    val table = Table(cols, true)

    for (i in 1..cols)
        for (j in 1..cols)
            table.addCell("($i, $j)")

    return table
}