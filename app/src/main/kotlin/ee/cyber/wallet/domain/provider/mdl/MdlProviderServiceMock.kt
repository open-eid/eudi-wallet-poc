package ee.cyber.wallet.domain.provider.mdl

import android.content.Context
import ee.cyber.wallet.crypto.CryptoProvider
import ee.cyber.wallet.domain.credentials.Credential
import ee.cyber.wallet.domain.credentials.CredentialIssuanceService
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.pid.MOCK_APP_PREFS
import ee.cyber.wallet.domain.provider.pid.MOCK_USER_PID
import ee.cyber.wallet.domain.provider.wallet.KeyType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.util.*

class MdlProviderServiceMock(
    private val context: Context,
    private val cryptoProviderFactory: CryptoProvider.Factory,
    private val credentialIssuanceService: CredentialIssuanceService
) {
    private val PORTRAIT_38001085718 = context.resources.openRawResource(ee.cyber.wallet.R.raw.portrait_38001085718).readBytes()
    private val PORTRAIT_47101010033 = context.resources.openRawResource(ee.cyber.wallet.R.raw.portrait_47101010033).readBytes()
    private val PORTRAIT_50801139731 = context.resources.openRawResource(ee.cyber.wallet.R.raw.portrait_50801139731).readBytes()
    val SIGNATURE_USUAL_MARK = Base64.getDecoder().decode(
        "/9j/4AAQSkZJRgABAQEAsgCyAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMUFRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQUFBQU" +
            "FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBT/wAARCACXAMgDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIh" +
            "MUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXG" +
            "x8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAV" +
            "YnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq" +
            "8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9OqKOKOaYB0paSloAKKKKACiiigAppp1IaAE6c0tITxS0AJ1pSKMUUAA4oopaAEopaKAEpaKKACiiigAooooAaRkmijrRQAuKBRRQAUtFJQAtJupsjhFZmIVVGST0FeU3" +
            "PxS1Xxxr1xo3gG1iuYbVzHea9dgm0iYdUTH+sYe3A9aaTZLko7nrG6jNeEaz8UvFfwg8e+H9M8ay2eqeG9emFnb6vawmE21yfupIpJ4boD617qCCARyKGrApJjqWmjrS0igxR2paa1AATSg18zap8R/EFt+3PpPh" +
            "FdRlHh6fw+87WPGwy5J3fXivphabViU7jqKKKRQUUUUAFFJmjNAC0UmaTOaAFzij3rlfHfxJ0L4d2Cz6vdbZpTst7OIb57h+yog5Y1mfDzW/GXia7uNU1zTINC0WVB9j0+Q7rsf7UhHAz/dGcU7O1yeZXsd71opM" +
            "0UFDqSlopAFNJzQTXlvx/wDibeeAfDFrp2gxrd+MvEE407RrQ95W+9K3+xGuWJ9vejyQm7K7MD4geI9R+LnjWb4ceGLqS00y0Cv4j1i3ODCh5FtG3/PRh1/uj6ivXfDfhrTfCGiWuk6Tax2djbIEjijGAB/U+9c7" +
            "8Ifhna/CrwXa6PFK13fuTcahfycyXdy/MkjH3P5DArtcVbdvdREVf3meK/ti+EW8W/s9+KhAp+36bANStXX7ySQkOCP++TXZfA7xmPiH8IvCXiINue/06GWQ/wC3tAYfmDXR+LNMTWvCusafIu5LqzlhYeoZCP61" +
            "8/f8E8tXe9/Zys9PlYmTR9Su7A7uoCyFgPycULWLQPSaZ9LilpB1pag0AUh60ppCKAPjr4hn+zP+CingCYkgXmiyRD0PEv8AhX2KBzXyF+07Aug/tb/AjXyCFuLiSwZh6lgB/wCjK+vT3rSXQiPUUnFQC+tzcm3E" +
            "8f2gDJi3jcB9OtS9a87l+A/hD/ha6/Ec29zH4lWPy2mW7cRMNu3mPO08VCRTv0PRc0E965/wh8QPDnj+K/l8O6xaaxHY3DWly1pIHEUo6o3oa+Pf+ClPxf8AGPwxfwFF4U1m70o3Es1xOloxUzeWUwGx1HJ4pqLe" +
            "gnJLU+355DHDI4GSqkgfhXyp+y1+2Td/Gf4p+MPA/iKytNN1HTppDp5tyR50aOVZTn+IcHj19q91+GPxK0/4lfCXSPGFnOrW15p4nlOf9W4X94p9CrAg/SvjL9nX9mC2+Jvw+uviX4c1Sfw18Qjr91dadq+4tEyK" +
            "5AjdOhRstn/IppJxE209D7c+J3xI0f4S+CdR8T668iadZKCyxLudyTgKo7kk15TffHjxt8QLKCy+Hfw+1a0u7tQTq3iWH7La2qn+LGdzn0AFZFz8F/il8Y/FGgf8LUv9CtfCmizrdnS9CMj/ANozp91pS4GFB528" +
            "19LKgjUKihVUYAHQCnpEWsvJHmHw0+CFt4SvTr/iK/l8VeMp1/fareDIiz1SFOka/Tn1NeodaUCjvUN3LSS2ExRS0Uhi0lHekJoAjnnjtoJJpnEcUal3djgKAMkmvh74E/tCW3xs/bU1S41PTJE07+y57fwjcz7g" +
            "hijkxNJGCMMZNrEsOgTbXtH7YnizUoPAuleAfDsxi8UePL5dFtWU/NDAebib6LHwT23V5d+0/wCEdN/Z91X9nzxjosP2TSfCWqx6DdMgwTaTKAS34LISfVz61pTWt36ff/SMaj0sump9m9KU80mQwyDkHvRUGw2V" +
            "d0bg9CpFfI3/AATmvg/hT4mWAYkWviy5YDPZlX/4k19a3k621nPM5wkcbOT6ADNfCn/BLnWmv5fizC+4GTUbe9Abv5hnyf8Ax0VUeq8v1Il0fmfeVApBS5qCwzg0GjNHagD5I/4KEKdD0f4Y+LUDZ0XxPAWZeoVv" +
            "m/8AadfWkUgmiSRTlWUMD9a+cP8AgoZpA1L9lbxPc8iXTp7S7jYdiJ0Q/o5r1v4I+KV8bfB7wVriv5jX2kWszt/tmJd4/wC+s1b2RK3Z0Ol+LNE1zUb7T9O1exv7+wYLdW1tcJJJAfR1BJX8a+Pv+ConjzWfBnw4" +
            "8IRaDrWoaPfXepSM32CdojJGkeTuKkEgErx0rxn9kTxza+Cv29/HunXLGGy8Q32p6bbyNwhmW58xBnpkhCo92HrX0F8bY9O+Jv7bHw78IXtuuo6Z4e0G/wBU1K2cbkAmidAGH4If+BCqS5ZEN3ieG/8ABLz4mT+H" +
            "fFWseF9eE9uni2M6jpl1cghbqaJnWUKx+8T834xkV9B/F2O08cftwfC7w68SXkejaReajeQyKGQK6sqhh+A/MV4F+xnpfhj9pb4VX3w4v55/D/ibwTqD6p4d1iwfF3bQSylsg/xbZPvdM7k6EZr67+CH7Na/Czxf" +
            "r3jLXvFV9458Z6vGttLq1/EsXlwLjEaICQPurk57DAHOU9G2NdDiNd/ZM8XeGU8R6R8LvHcXhbwh4jLm80i9tDOLMuMSNbMCNuRxjt617j8Ifhjpvwc+HWi+EdKd5rXTodhmk+9K5JZ3PuWJP412NLUt8xSVhKcK" +
            "SjNSULRQKKACijvRTAM03vRWF488WW3gPwRr/iO7/wCPbSbGa9ceojQtj8cY/GpbsrhueCeCc/F/9szxd4jk/faH8PLBNB07P3TfTfNcOP8AaUBoz+FdP+2z4Ibx7+zF46soo/MurOzGpwYGSGt2Epx7lVYfjUH7" +
            "E/hO68P/AAE0rVtTy2t+KZ5vEN/K3V3uG3KT/wBswn617hqFjDqlhc2Vwgkt7iJoZEPRlYEEfkauaaXL1X5/8OTHXV9fy/4Y4L9nTx6vxN+BngjxJvMkt7pcInYn/lsi+XL/AOPq1ei18n/8E79Rk0bwF42+Hd3I" +
            "XvvBniS6sip/hidiV/N0mr6wpy3v3FDRWfQ87/aJ8YR+A/gZ451t5PKe30qdImzj966mOMfi7rXzt+xx4VPw3+NOteHWTypbrwRouoTJjH73btk/8edq7v8Aajm/4Wl448A/BiyzN/a16mta8EPEOm27bsN6eY4C" +
            "j3UetW4Y/wCzf2+J9o2x3/w+U+25L7H8qI9fMbVz6HrzP45ftCeFv2fNK0vUPFMepvbahObeN9Os2nCEAElyCAo5HfJ5wDg46r4ieOdP+GfgbXfFeqpNJp2j2kl5OlsoaRlQZIUEgEnpyRXhnh79su3+IP7NPjP4" +
            "reGfCV5KfD0kkR0vUJlTzdgjZn3qCNqpJuIxn5SPQ0kmwbPojRdXtfEGj2OqWTtJZ3sEdzA7oyMyOoZSVYAg4I4IBFfnneftXfE/wv8AtI+LPEl7qzXXwo0Xxavhi+01o18u3jcyIsi4XIIETPuzycA8GvsL9nH4" +
            "+af8fvg5YeOBDFpT/vYtRtfO3raSxn5gWIHBXa4z2YV8AeGdXsPix8DP2sWsrh0E+sQeILbPBaL7VI+SOvQY/EVcUle4nd2sfaf7fWpQ6d+yX48MrqDcRW0EYJ+8zXMWMfhk/hXjv/BMb48WWsfC3Ufh9q12sOqe" +
            "GWkurUTNgyWTsWbGevluWz6B1roPhf8Asp6h8Tvh18Nrnxb8UNd8UeA47Oy1mLwrf20RJmMKsEkuR87xqWICkcDgEda4jw7+xxp2oftJ/Efw34l8M65B4c1J21rQ/FmjO1stsshbzbQyYKkMJNu0gkeVwAGJo0tY" +
            "LO9x/wCzF8B9H/aP+A3ibVdTlutH1a68Z3mr6Nrtn8tzZyBYsOh4yNwIIzztHIIBHv3wf/Zkk+FOseMvGOv+Lrzx143160NtLrF3brB5cKqMRqgZsZ2pk5/gXAHOfV/h18PdC+FXgzTPC3hu0+xaPp8eyGMsWYkk" +
            "szMx6szEkn1NdGwDAg8gjGKnmHyn5efB+0/4Ur8Lvgr8ctMiMVtp+oXvh/xSI1/1tnNdy7ZHA6lMnBPcR+lfqFDNHcQxzROskUih0dDkMCMgg9xXx3+y18OrP4hfs0fE74XaqAEtvEGp6VlxnymGxo5Mf7MnzfVa" +
            "9D/Yi8d3/iP4Pf8ACL66WXxN4Kun0C/SQ5fEXETH/gHyZ7mNqTfUaVj6Eoo6UlIYUUjMFUkkADkk9q4q9+K2n3FzLZeG7K88XX6MUZNKUG3iYdRJcMREpHpuLexoA7bNR3N1DaQtLPNHBEvV5GCqPxNcLNoPjXxL" +
            "E8ms+IYPCVjjJs9AVZZgv+3dTLj/AL5jXH96vJfEuh+G/FUk+keBPCUPxF17mGTxH4pnk1DTbBujFpZ2cOw6+XEP8KaQtT6P0/VbLV4WlsLy3vYlbaXt5VkUH0yD1oryj9mj9m/Sf2cPCV5p1nevqWqanMLnUbwo" +
            "I43kAICxxjhEXJx35+gBUjPYDXgP7bd3cz/BA+GrGQx33irVrHQ4ivX97MGb/wAdRgfY178a8F+OUI8S/Hn4JeHWy0MOoXmuSrjIBt4f3ZP/AAJjTW6XmHRnt2j6Vb6DpFlptmgitLOBLeFB/CiKFUfkBVyiuN+J" +
            "Pxj8F/CCyt7nxh4htNDjud32dJyTJMVxuCIoLNjI6DuKL3ElbRHzz8P3Pwz/AOChPxA0BgYdP8baJBq9svQPPHgN+JxcGvfPjN8ZdA+CPhCTXNblMksjCCx06Ejz764P3Io17knqegHJr4e/a3+KNx8QPiF8LvH3" +
            "gbRde0m0sr86OfEer28+m2l355+RFdGSfYB525l28McHNfS/wp/Yz8L+F7t9e8cyn4j+LZ8sbrXN11bWgJzst4pmfAH95iWOOo5FVa8VfpoGzY/9njSrDRtT1zxj4y8SaHcfEzxY6yXlpBqEUn9n26/6myi+YnCD" +
            "G7HVvXAJteLYRaftneAbwZ/03wvqFpnsdkqSf1r02/8AhH4F1Syazu/BmgXFqw2mJ9MhK4/754r5R+N3h4fst/HH4a+MfDaatq3hhEv47nw21w88dhD5a+bLbbiSihTuKZ2jZ2B4LpspHv8A+1vd2Nl+zP8AEmTU" +
            "ZxbW7aJcRByM5kddsa/i5UfjXzb+yaNLtf8AgnB43YDhrLW/tgP/AD08llA/748uux/a38UWnxu1X4VfCTQLs3dl4zu4dY1Ca3PTTY/nBPs2GYe8QrmPEH7LXxU8E2vjr4a/DyLTG+GXjO+S6W9urrZJoyMR50Yj" +
            "6uCqqnGflUdCTTWisTZNmf8As2fsa2vi74BeHdS0/wAc+JvCNt4nsm/4SHS9NnHkainmuFwG/wBWSmFJGQQcY5Oet+L37Cd/NqBb4Ravp3g/TNT0aPQNa028Ryk9ujIRIpVWJkIRQScE4Jz8xr6r8EeErTwH4O0T" +
            "w5YZ+x6VZRWURIwWVEC5PucZ/GtupcmPbYw/AvhSDwL4K0Dw3bSmaDSLCCxSVhguI4wgYjtnbmtyil61ICUUoNJQB81fAFB4M/ac+N/hOQlF1C5t9ftU6ArIuZWH/AplH/AapeSfgv8Att7kYw+H/iVp/wA6dEF/" +
            "COv1OPzuDWp8cE/4Vn+0R8NviMAY9M1Ld4a1ZxwAHy0LN7AsxJ/6ZisL9t/XYT4U8O+IvD8Ump6z4S123vPtMA/cQAtgo8vQZcR5UZPHSqte/wB5a6fcfVUjrEjO7BUUZLMcAD1rjZ/iBLrE5tfCmmvrsmSrag7e" +
            "TYRH3lIPmfSMN74pmneEx4xtLPVPEV8dYgnjSeLTogY7FQwDLlOsp56uSPRRWrrXjHTfD0senW8MmoakVAi0zTkDyAdsjgIvuxApLyIMn/hW0viCRJ/F+qy67g5GmQg2+nr7GIEmX/tozD/ZFPv/AB3p2kSjQ/DO" +
            "mtrmpQAILDTAqQWw7ebJwkQ9vveimiTwxrfjABvEV82mac3/ADB9LlKlx6TTjDN7qm0e5rqtK0mx0OwjstOtIbG0jGEhgQIo/AUwOHk+G2oeM5Fn8c6n9ttQdy+H9OZo7FfQSnh5z/vYX/ZrvLKxttNtIrSzt4rS" +
            "1iUJHDAgREHoAOAKnNFK4CdaKWikAleLXkI1P9r6wLZK6Z4RkkUHorS3IX+S17TXk+lWTn9p7xBdlJfLHhq1iDlDsGZ5DgN68dKqO6+f5MHs/wCuqPWKpX+iabqs9rNfafa3k1qxeCS4hV2hY9ShI+U/SrtLipA8" +
            "Q/bO8D/8J3+zp4qt4kJutPRNTt2UfMjQuHYj32bx+Nd78G/GQ+IHwq8K+Id2+W+0+GSbBziUKBIPwYMK6rULKHU7C5s7hBJb3ETRSI3RlYEEfka+fP2Nbqbw5ofi74d3zsbzwprE0UYbqbeRi0ZHsSHP4imuq+Y3" +
            "smfRVePfGXT4rz4o/CkXCCS2nu9QsZUIyGWWzcEH2OK9hryv4yJnxl8LHyQU8Qdve3lFC3BHC/s4fsc23wI8Yal4l1DxLceK9Sa3+waa1zEUFhabs+WuWbnoOMADOBya+j80GgCk22IMUUtGKAEpe1ITVe9vrfTr" +
            "d57qZIIV6u5wKALFY+teKLTR5Ftwkt9qLj93ZWq75W9z2Uf7TECqbXWq+Jvls9+kacet1Iv+kSD/AGFP3R7tz7d6u2dhpXhCwllBS1i+/NczPlnPqznlj9adrAedfGb4P6j8c/hzq2h6zdpp5mj82zsrVsrHOvMZ" +
            "lk6tyMELgYJ618+fE744aFcfsh614X8SzRaP4z0trfSZ9GZQJ5ZYpkIeOMfeBVCSQMZB7Yr62Or6v4mJTRoTptieDqV5H8zD1iiPJ/3mwPY1wfiX9kf4beM/Elr4h13SZdS1yJ1kmvZblw1yR08wA4I4HAA446Vd" +
            "7bh5F34WWfijxN8MfCcN+ZfC9oml2qPHG6vezYiUHLcrFn2y3upr0fRPD+neHLU2+n2y26MdztyzyN/edjyx9ySavpGkUaoihUUBVUcACn4zUX6D3ExQBSgdKKQgxSYpaKACiiikAmK81v8Aw34mg+Pum69YgSeG" +
            "LjRnstQDygbJVk3REL3PzMM+lel44pKadncOlhKX6UEc0tACEV87eLcfCb9qjQ/ERzFo3jW1/sq7PRVukx5TH64Ufia+ijXm37QHw1b4nfDa+sLY+Xq9oVvdOmH3knj5XB7Z6fjQnZ3GtdD0c15l8VF+0ePPhlAD" +
            "z/a8k2Mdlt5P8aufAv4lx/FHwBZ6hKPJ1e2zaalatw0NwnDgjtnqPY1T8QONa+PHhmxQ7l0jT7i+lHo0hEafpu/KrSsxI9OJ5pabTugqADNHbNV76/t9Nt2muJBFGPXqT6AdzWQFv/EYJfzNN049EBxNKPc/wD9f" +
            "pTSAlvdfJuDZ6bD9vvBw2DiKL3du30HNFl4dH2lb3Upjf3i8qWGIov8AcToPqeafdajp3hi2jt0TDniK1gXdI59h/U1T/svUvEXzam7WFkeljA/zuP8Apo4/kPzqhD73xOZblrLR7f8AtG7Bw8gO2CE/7b+vsMmi" +
            "z8KLJdJe6xOdUvVOUVxiGE/7CdM+5ya27WzgsbdILeJIYkGFRBgCpcVN+wxMfgKXFFFIAx+dFLSUgCjvS0UwCkpaMUgEop2OKKYDaMUtFIBKBS0UAJikIpaWmB4h4s+FXifwf48uPGvw1e0+0ahgatoV85jt7wjp" +
            "IrDOxx645rp/hb4M1yw1bXPFXis26+IdYZENtaOXitIEGEiViBnqSTgcmvRyKQDNO7tYBKztT1pbORba3jN3fP8AdhTt7sewqrqWtS3F0+n6YA9wo/fXDf6uAe/qfasyz1e1013sdHifWdRY/vpwflDerv0/AVSi" +
            "K5qRadFZsdS1i5SWdBkM5xHCPRQf59ahOqah4gJTS0Nnad76deWH+wp6/U8U608MyXV0t7rE/wBsnXlIF/1MX0U9T7muhAAGAOPak2Bm6ToFrpG50BmuX/1lzKd0jn3P9BxWlilAoxU3uMSilxSYpAFFGKKACijF" +
            "Lj1oASjFLigdKAACjFLjmigBBRS9aKYDaKKKQBRRRQAlLRRQAVDdxST2sscUhhkdCqyAZ2nHWiimB5f4K+C2o6LZm21/xhqPiC3855fI2rAjlmJ+cr8zenXHtXp1jp9tptusFrAlvCowEjXAFFFNtvcLWLFL60UU" +
            "lqAdhS0UUgCkoooAMUEZoopgGKWiigAooopAFFFFMAooopAf/9k="
    )
    val BIOMETRIC_TEMPLATE_IRIS = Base64.getDecoder().decode(
        "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMUFRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQU" +
            "FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBT/wAARCAAjACMDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQID" +
            "AAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2" +
            "t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEI" +
            "FEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU" +
            "1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9GtX8U6hFqVxFDKIY43ZAoRT0OM8iqn/CWar/AM/X/kNP8Kp6x/yFr3/ru/8A6EaqrywoA1v+Es1X/n6/8hp/hSr4t1QMCbkMB2Ma8/pT" +
            "PE9tFaavJHCgjjCqQq9OlZVAHqllObqzgmI2mSNXIHbIzRUWj/8AIJsv+uCf+giigDzvWP8AkLXv/Xd//QjVVfvD61a1j/kLXv8A13f/ANCNVAcHNAG14v8A+Q5L/ur/ACrFq1qWoSapdNcSqquwAwgIHFVa" +
            "APT9H/5BNl/1wT/0EUUukcaTZf8AXBP/AEEUUAQ3fh7T72ZpprYNI3VgzLn8jUP/AAielf8APr/5Ef8AxoooAP8AhE9K/wCfX/yI/wDjSr4V0pWBFqMj1dj/AFoooA1QAoAAAA4AFFFFAH//2Q=="
    )
    val BIOMETRIC_TEMPLATE_SIGNATURE_SIGN = Base64.getDecoder().decode(
        "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMUFRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQU" +
            "FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBT/wAARCAAkACIDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQID" +
            "AAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2" +
            "t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEI" +
            "FEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU" +
            "1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9GtX8U6hFqVxFDKIY43ZAoRT0OM8iqn/CWar/AM/X/kNP8KuW9pBJqOt3U0X2j7M7ssRPDEseT+VUZNZt7hHSXTLZQQQrQgoynsfegB3/" +
            "AAlmq/8AP1/5DT/ClXxbqgYE3IYDsY15/SpbSO307QkvmtUu5pZSn70ZVAPb8KpXmpQXkBH2GGCbIIkhyo+hFAHollObqzgmI2mSNXIHbIzRUWj/APIJsv8Argn/AKCKKAODur+fTtcvZbeQxt5zg9wRuPBF" +
            "aGnS2/iOZ7a4tIopyhZZ4BtOR6jvVO41OTTtX1ELHFNHJM26OZNynDHH86H8RyJFIlta29oZBtZ4kw2PrQBWsNYudNVkjZWiflopF3Kfwq/LHbato9zdpbJaXFuVz5XCOCfTtVOz1l7W3EElvb3MSklRMmSP" +
            "oaW+1yW7tvsyQw2tvnJjhXG4+9AHe6P/AMgmy/64J/6CKKXSONJsv+uCf+giigCG78PafezNNNbBpG6sGZc/kah/4RPSv+fX/wAiP/jRRQAf8InpX/Pr/wCRH/xpV8K6UrAi1GR6ux/rRRQBqgBQAAABwAKK" +
            "KKAP/9k="
    )
    val BIOMETRIC_TEMPLATE_FINGER = Base64.getDecoder().decode(
        "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMUFRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQU" +
            "FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBT/wAARCAAdACADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQID" +
            "AAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2" +
            "t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEI" +
            "FEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU" +
            "1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9BbbRb68hWWG3eSNujDHNNu9Ju7FFaeBolY7QT3NbDW91ceF9PFqkjsJHz5ec4yfSsi5tb23MZuo5kUtx5gOM/jQBN/wjmpf8+cn6U3TY" +
            "JrPXLNJUeGQTICGGDgkVc8WTSJrs4V2UALwDj+EVduCzt4bebJuC43FvvEblxmgCrczyweFtOMUjxkyPkoxGeTWM1zNOyCWV5ADwHYnFQ0UAdT4j128sdXlhhdFRQpGY1J6DuRWRZ3s9/rllLcSGV/OjGT2+" +
            "YVm1c0YZ1ey/67J/6EKAP//Z"
    )
    val BIOMETRIC_TEMPLATE_FACE = Base64.getDecoder().decode(
        "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMUFRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQU" +
            "FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBT/wAARCAAgACIDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQID" +
            "AAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2" +
            "t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEI" +
            "FEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU" +
            "1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9BtcmebV7wuxbbKyjPYAkAVVjglmz5cbyY67VJxU+sf8AIWvf+u7/APoRrU0KaW30PVZIWZJV8vBXqOaAMV7SeNSzQyKo6koQBUasUYFS" +
            "QRyCO1Xp9V1GeFkmnmaNhhg3Q1QoA9S0yRptNtJHO53iRmJ7kgUUzR/+QTZf9cE/9BFFAHnesf8AIWvf+u7/APoRrU0K5ktND1WaFyki+Xhh25rO12F4dXvA6ld0rMM9wSSDVRJ5I43jWR1jfG5AxAb6jvQB" +
            "cuddv7uFoZrlnjbqpA5/SqFFKqlmAAJJ6AUAenaP/wAgmy/64J/6CKKfpkbQ6baRuNrpEisD2IAooA//2Q=="
    )
    private val MDL_38001085718 = Credential.MdocMdlCredential(
        familyName = "Joeorg",
        givenName = "Jaak-Kristjan",
        birthDate = LocalDate.parse("2005-01-08"),
        issueDate = LocalDate.parse("2020-12-14"),
        expiryDate = LocalDate.parse("2028-12-14"),
        issuingCountry = "EE",
        issuingAuthority = "RIA",
        documentNumber = "EE1234567",
        portrait = PORTRAIT_38001085718,
        drivingPrivileges = listOf(
            Credential.DrivingPrivilege("A", LocalDate.parse("2020-12-14"), LocalDate.parse("2025-12-14"), listOf(Credential.DrivingPrivilege.Code("C", "S", "V"))),
            Credential.DrivingPrivilege("B", LocalDate.parse("2020-12-14"), LocalDate.parse("2028-12-14"))
        ),
        unDistinguishingSign = "EST",
        administrativeNumber = "38001085718",
        sex = 1,
        height = 185,
        weight = 85,
        eyeColor = "blue",
        hairColor = "brown",
        birthPlace = "Tallinn",
        residentAddress = "Tallinna mnt 605, 10145 Tallinn",
        portraitCaptureDate = LocalDate.parse("2020-12-14"),
        ageInYears = 20,
        ageBirthYear = 2005,
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = false,
        issuingJurisdiction = "EE-I",
        nationality = "EE",
        residentCity = "Tallinn",
        residentState = "Harju",
        residentPostalCode = "10145",
        residentCountry = "EE",
        biometricTemplateFace = BIOMETRIC_TEMPLATE_FACE,
        biometricTemplateFinger = BIOMETRIC_TEMPLATE_FINGER,
        biometricTemplateSignatureSign = BIOMETRIC_TEMPLATE_SIGNATURE_SIGN,
        biometricTemplateIris = BIOMETRIC_TEMPLATE_IRIS,
        familyNameNationalCharacter = "Jaak-Kristjan",
        givenNameNationalCharacter = "Jõeorg",
        signatureUsualMark = SIGNATURE_USUAL_MARK
    )

    private val MDL_38001085718_1 = Credential.MdocMdlCredential(
        familyName = "Joeorg",
        givenName = "Jaak-Kristjan",
        birthDate = LocalDate.parse("2005-01-08"),
        issueDate = LocalDate.parse("2020-12-14"),
        expiryDate = LocalDate.parse("2028-12-14"),
        validUntil = Instant.parse("2025-01-01T00:00:00Z"),
        issuingCountry = "EE",
        issuingAuthority = "RIA",
        documentNumber = "EE1234567",
        portrait = PORTRAIT_38001085718,
        drivingPrivileges = listOf(
            Credential.DrivingPrivilege("A", LocalDate.parse("2020-12-14"), LocalDate.parse("2025-12-14"), listOf(Credential.DrivingPrivilege.Code("C", "S", "V"))),
            Credential.DrivingPrivilege("B", LocalDate.parse("2020-12-14"), LocalDate.parse("2028-12-14"))
        ),
        unDistinguishingSign = "EST",
        administrativeNumber = "38001085718",
        sex = 1,
        height = 185,
        weight = 85,
        eyeColor = "blue",
        hairColor = "brown",
        birthPlace = "Tallinn",
        residentAddress = "Tallinna mnt 605, 10145 Tallinn",
        portraitCaptureDate = LocalDate.parse("2020-12-14"),
        ageInYears = 20,
        ageBirthYear = 2005,
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = false,
        issuingJurisdiction = "EE-I",
        nationality = "EE",
        residentCity = "Tallinn",
        residentState = "Harju",
        residentPostalCode = "10145",
        residentCountry = "EE",
        biometricTemplateFace = BIOMETRIC_TEMPLATE_FACE,
        biometricTemplateFinger = BIOMETRIC_TEMPLATE_FINGER,
        biometricTemplateSignatureSign = BIOMETRIC_TEMPLATE_SIGNATURE_SIGN,
        biometricTemplateIris = BIOMETRIC_TEMPLATE_IRIS,
        familyNameNationalCharacter = "Jaak-Kristjan",
        givenNameNationalCharacter = "Jõeorg",
        signatureUsualMark = SIGNATURE_USUAL_MARK
    )

    private val MDL_38001085718_2 = Credential.MdocMdlCredential(
        familyName = "Joeorg",
        givenName = "Jaak-Kristjan",
        birthDate = LocalDate.parse("2005-01-08"),
        issueDate = LocalDate.parse("2020-12-14"),
        expiryDate = LocalDate.parse("2025-01-01"),
        issuingCountry = "EE",
        issuingAuthority = "RIA",
        documentNumber = "EE1234567",
        portrait = PORTRAIT_38001085718,
        drivingPrivileges = listOf(
            Credential.DrivingPrivilege("A", LocalDate.parse("2020-12-14"), LocalDate.parse("2025-12-14"), listOf(Credential.DrivingPrivilege.Code("C", "S", "V"))),
            Credential.DrivingPrivilege("B", LocalDate.parse("2020-12-14"), LocalDate.parse("2028-12-14"))
        ),
        unDistinguishingSign = "EST",
        administrativeNumber = "38001085718",
        sex = 1,
        height = 185,
        weight = 85,
        eyeColor = "blue",
        hairColor = "brown",
        birthPlace = "Tallinn",
        residentAddress = "Tallinna mnt 605, 10145 Tallinn",
        portraitCaptureDate = LocalDate.parse("2020-12-14"),
        ageInYears = 20,
        ageBirthYear = 2005,
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = false,
        issuingJurisdiction = "EE-I",
        nationality = "EE",
        residentCity = "Tallinn",
        residentState = "Harju",
        residentPostalCode = "10145",
        residentCountry = "EE",
        biometricTemplateFace = BIOMETRIC_TEMPLATE_FACE,
        biometricTemplateFinger = BIOMETRIC_TEMPLATE_FINGER,
        biometricTemplateSignatureSign = BIOMETRIC_TEMPLATE_SIGNATURE_SIGN,
        biometricTemplateIris = BIOMETRIC_TEMPLATE_IRIS,
        familyNameNationalCharacter = "Jaak-Kristjan",
        givenNameNationalCharacter = "Jõeorg",
        signatureUsualMark = SIGNATURE_USUAL_MARK
    )

    private val MDL_38001085718_3 = Credential.MdocMdlCredential(
        familyName = "Joeorg",
        givenName = "Jaak-Kristjan",
        birthDate = LocalDate.parse("2005-01-08"),
        issueDate = LocalDate.parse("2020-12-14"),
        expiryDate = LocalDate.parse("2028-12-14"),
        issuingCountry = "EE",
        issuingAuthority = "RIA",
        documentNumber = "EE1234567",
        portrait = PORTRAIT_38001085718,
        drivingPrivileges = listOf(
            Credential.DrivingPrivilege("A", LocalDate.parse("2020-12-14"), LocalDate.parse("2025-12-14"), listOf(Credential.DrivingPrivilege.Code("C", "S", "V"))),
            Credential.DrivingPrivilege("B", LocalDate.parse("2020-12-14"), LocalDate.parse("2025-01-01"))
        ),
        unDistinguishingSign = "EST",
        administrativeNumber = "38001085718",
        sex = 1,
        height = 185,
        weight = 85,
        eyeColor = "blue",
        hairColor = "brown",
        birthPlace = "Tallinn",
        residentAddress = "Tallinna mnt 605, 10145 Tallinn",
        portraitCaptureDate = LocalDate.parse("2020-12-14"),
        ageInYears = 20,
        ageBirthYear = 2005,
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = false,
        issuingJurisdiction = "EE-I",
        nationality = "EE",
        residentCity = "Tallinn",
        residentState = "Harju",
        residentPostalCode = "10145",
        residentCountry = "EE",
        biometricTemplateFace = BIOMETRIC_TEMPLATE_FACE,
        biometricTemplateFinger = BIOMETRIC_TEMPLATE_FINGER,
        biometricTemplateSignatureSign = BIOMETRIC_TEMPLATE_SIGNATURE_SIGN,
        biometricTemplateIris = BIOMETRIC_TEMPLATE_IRIS,
        familyNameNationalCharacter = "Jaak-Kristjan",
        givenNameNationalCharacter = "Jõeorg",
        signatureUsualMark = SIGNATURE_USUAL_MARK
    )

    private val MDL_47101010033 = Credential.MdocMdlCredential(
        familyName = "O’Connež-Šuslik",
        givenName = "Mari-Liis Õnne",
        birthDate = LocalDate.parse("1971-01-01"),
        issueDate = LocalDate.parse("2020-11-13"),
        expiryDate = LocalDate.parse("2028-11-13"),
        issuingCountry = "EE",
        issuingAuthority = "RIA",
        documentNumber = "EE2345678",
        portrait = PORTRAIT_47101010033,
        drivingPrivileges = listOf(
            Credential.DrivingPrivilege("A", LocalDate.parse("2020-11-13"), LocalDate.parse("2025-11-13"), listOf(Credential.DrivingPrivilege.Code("C", "S", "V"))),
            Credential.DrivingPrivilege("C", LocalDate.parse("2020-11-13"), LocalDate.parse("2028-11-13"))
        ),
        unDistinguishingSign = "EST",
        administrativeNumber = "47101010033",
        sex = 2,
        height = 175,
        weight = 55,
        eyeColor = "blue",
        hairColor = "brown",
        birthPlace = "Tallinn",
        residentAddress = "Pärnu mnt 705, 12145 Tallinn",
        portraitCaptureDate = LocalDate.parse("2020-11-13"),
        ageInYears = 54,
        ageBirthYear = 1971,
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = true,
        issuingJurisdiction = "EE-I",
        nationality = "EE",
        residentCity = "Tallinn",
        residentState = "Harju",
        residentPostalCode = "12145",
        residentCountry = "EE",
        biometricTemplateFace = BIOMETRIC_TEMPLATE_FACE,
        biometricTemplateFinger = BIOMETRIC_TEMPLATE_FINGER,
        biometricTemplateSignatureSign = BIOMETRIC_TEMPLATE_SIGNATURE_SIGN,
        biometricTemplateIris = BIOMETRIC_TEMPLATE_IRIS,
        familyNameNationalCharacter = "O’Connež-Šuslik",
        givenNameNationalCharacter = "Mari-Liis Õnne",
        signatureUsualMark = SIGNATURE_USUAL_MARK
    )

    private val MDL_50801139731 = Credential.MdocMdlCredential(
        familyName = "Alaealine",
        givenName = "Alar",
        birthDate = LocalDate.parse("2008-01-13"),
        issueDate = LocalDate.parse("2020-10-14"),
        expiryDate = LocalDate.parse("2028-10-14"),
        issuingCountry = "EE",
        issuingAuthority = "RIA",
        documentNumber = "EE3456789",
        portrait = PORTRAIT_50801139731,
        drivingPrivileges = listOf(
            Credential.DrivingPrivilege("A", LocalDate.parse("2020-10-14"), LocalDate.parse("2025-10-14"), listOf(Credential.DrivingPrivilege.Code("C", "S", "V")))
        ),
        unDistinguishingSign = "EST",
        administrativeNumber = "50801139731",
        sex = 2,
        height = 185,
        weight = 85,
        eyeColor = "brown",
        hairColor = "brown",
        birthPlace = "EE",
        residentAddress = "Tartu mnt 605, 13145 Tallinn",
        portraitCaptureDate = LocalDate.parse("2020-10-14"),
        ageInYears = 17,
        ageBirthYear = 2008,
        ageOver16 = true,
        ageOver18 = false,
        ageOver21 = false,
        issuingJurisdiction = "EE-I",
        nationality = "EE",
        residentCity = "Tallinn",
        residentState = "Harju",
        residentPostalCode = "13145",
        residentCountry = "EE",
        biometricTemplateFace = BIOMETRIC_TEMPLATE_FACE,
        biometricTemplateFinger = BIOMETRIC_TEMPLATE_FINGER,
        biometricTemplateSignatureSign = BIOMETRIC_TEMPLATE_SIGNATURE_SIGN,
        biometricTemplateIris = BIOMETRIC_TEMPLATE_IRIS,
        familyNameNationalCharacter = "Alar",
        givenNameNationalCharacter = "Alaealine",
        signatureUsualMark = SIGNATURE_USUAL_MARK
    )

    val pidToCredential = mapOf(
        "38001085718" to MDL_38001085718,
        "1" to MDL_38001085718,
        "11" to MDL_38001085718_1,
        "12" to MDL_38001085718_2,
        "13" to MDL_38001085718_3,
        "2" to MDL_47101010033,
        "3" to MDL_50801139731
    )

    suspend fun issueMdl(): Attestation {
        val pid = context.getSharedPreferences(MOCK_APP_PREFS, Context.MODE_PRIVATE).getString(MOCK_USER_PID, "") // TODO: This works only after PID credential issuing flow.
        val cryptoProvider = cryptoProviderFactory.forKeyType(KeyType.EC)
        return credentialIssuanceService.issueCredential(pidToCredential.getOrDefault(pid, MDL_38001085718), cryptoProvider.generateKey(KeyType.EC))
    }
}
