package io.tral909.test.telegram.bot.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@XmlRootElement(name = "ValCurs")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValCurs {

    @XmlElement(name = "Valute")
    private List<Valute> valute;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Valute {
        @XmlElement(name = "CharCode")
        private String charCode;
        @XmlElement(name = "Name")
        private String name;
        @XmlElement(name = "Nominal")
        private String nominal;
        @XmlElement(name = "Value")
        private String value;
        @XmlElement(name = "VunitRate")
        private String vunitRate;
    }

}
