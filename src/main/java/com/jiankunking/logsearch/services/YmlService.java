package com.jiankunking.logsearch.services;


import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.dto.FileBeatConfig;
import com.jiankunking.logsearch.dto.FileBeatTypeConfig;
import com.jiankunking.logsearch.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Service
public class YmlService {

    public String toYml(FileBeatConfig fileBeatConfig, String ip) {
        StringBuilder sb = new StringBuilder();
        sb.append("filebeat.inputs:");
        sb.append(GlobalConfig.LINE_BREAK);

        switch (fileBeatConfig.getFileBeatTypeConfig().getType()) {
            case docker:
                sb.append(getDockerTypeYml(fileBeatConfig.getFileBeatTypeConfig(), fileBeatConfig.getProject(), ip));
                sb.append(GlobalConfig.LINE_BREAK);
                break;
            case log:
                sb.append(getLogTypeYml(fileBeatConfig.getFileBeatTypeConfig(), fileBeatConfig.getProject()));
                sb.append(GlobalConfig.LINE_BREAK);
                break;
            case k8s:
                sb.append(getK8sTypeYml(fileBeatConfig.getFileBeatTypeConfig()));
                sb.append(GlobalConfig.LINE_BREAK);
                break;
            default:
                throw new IllegalArgumentException(" FileBeatTypeConfig type Invalid !");
        }
        sb.append("max_procs: " + fileBeatConfig.getMaxProcs());
        sb.append(GlobalConfig.LINE_BREAK);
        //默认取第一个
        sb.append(getHttpYml());
        sb.append(GlobalConfig.LINE_BREAK);

        sb.append(getLoggingYml());
        sb.append(GlobalConfig.LINE_BREAK);

        sb.append(getProcessorsYml(fileBeatConfig));
        sb.append(GlobalConfig.LINE_BREAK);
        //修改index需要指定 setup.template.name、setup.template.pattern
        sb.append("setup.template.name: \"filebeat\"");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("setup.template.pattern: \"filebeat-*\"");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("setup.template.settings:");
        sb.append(GlobalConfig.LINE_BREAK);
        if (fileBeatConfig.getEsHosts().size() > fileBeatConfig.getNumberOfShards()) {
            sb.append("  index.number_of_shards: " + fileBeatConfig.getEsHosts().size());
        } else {
            sb.append("  index.number_of_shards: " + fileBeatConfig.getNumberOfShards());
        }
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append(getOutput(fileBeatConfig));
        sb.append(GlobalConfig.LINE_BREAK);

        System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * 拼接docker type yml
     *
     * @param fileBeatTypeConfig
     * @param project
     * @return
     */
    private String getDockerTypeYml(FileBeatTypeConfig fileBeatTypeConfig, String project, String ip) {
        StringBuilder sb = new StringBuilder();
        sb.append("- type: docker");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  enabled: true");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  containers.ids:");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("   - '*'");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  fields:");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("    project: " + project);
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("    type: docker");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("    ip: " + ip);
        sb.append(GlobalConfig.LINE_BREAK);
        if (fileBeatTypeConfig.isMultiline()) {
            sb.append(joinMultilineYml(fileBeatTypeConfig));
        }
        return sb.toString();
    }

    /**
     * 拼接log type yml
     *
     * @param fileBeatTypeConfig
     * @param project
     * @return
     */
    private String getLogTypeYml(FileBeatTypeConfig fileBeatTypeConfig, String project) {
        StringBuilder sb = new StringBuilder();
        sb.append("- type: log");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  enabled: true");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  paths:");
        sb.append(GlobalConfig.LINE_BREAK);
        for (String path : fileBeatTypeConfig.getPaths()) {
            if (StringUtils.isEmpty(path)) {
                throw new IllegalArgumentException(" FileBeatTypeConfig path must have a value !");
            }
            sb.append("    - " + path);
            sb.append(GlobalConfig.LINE_BREAK);
        }
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  fields:");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("    project: " + project);
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("    type: log");
        sb.append(GlobalConfig.LINE_BREAK);
        if (fileBeatTypeConfig.isMultiline()) {
            sb.append(joinMultilineYml(fileBeatTypeConfig));
        }
        return sb.toString();
    }

    /**
     * 拼接k8s type yml
     *
     * @param fileBeatTypeConfig
     * @return
     */
    private String getK8sTypeYml(FileBeatTypeConfig fileBeatTypeConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append("- type: docker");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  enabled: true");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  containers.ids:");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("   - '*'");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  fields:");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("    k8sCluster: " + fileBeatTypeConfig.getK8sCluster());
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("    type: k8s");
        sb.append(GlobalConfig.LINE_BREAK);
        if (fileBeatTypeConfig.isMultiline()) {
            sb.append(joinMultilineYml(fileBeatTypeConfig));
        }
        return sb.toString();
    }

    /**
     * 拼接；
     *
     * @param fileBeatTypeConfig
     * @return
     */
    private String joinMultilineYml(FileBeatTypeConfig fileBeatTypeConfig) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(fileBeatTypeConfig.getMultilinePattern())) {
            throw new IllegalArgumentException(" FileBeatTypeConfig multilinePattern must have a value !");
        }
        sb.append("  multiline.pattern: '" + fileBeatTypeConfig.getMultilinePattern() + "'");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  multiline.negate: true");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  multiline.match: after");
        sb.append(GlobalConfig.LINE_BREAK);
        return sb.toString();
    }

    private String getOutput(FileBeatConfig fileBeatConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("output.elasticsearch:");
        sb.append(GlobalConfig.LINE_BREAK);
        //移除 index 中的%{[beat.version]}
        sb.append("  index: filebeat-%{+yyyy.MM.dd}");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  hosts: [" + this.esHostsToString(fileBeatConfig.getEsHosts(), ",") + "]");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("  pipelines:");
        sb.append(GlobalConfig.LINE_BREAK);

        switch (fileBeatConfig.getFileBeatTypeConfig().getType()) {
            case docker:
                sb.append("    - pipeline: docker-app-name");
                sb.append(GlobalConfig.LINE_BREAK);
                sb.append("      when.equals:");
                sb.append(GlobalConfig.LINE_BREAK);
                sb.append("        fields.type: \"docker\"");
                sb.append(GlobalConfig.LINE_BREAK);
                break;
            case log:
                sb.append("    - pipeline: log-timestamp-appname");
                sb.append(GlobalConfig.LINE_BREAK);
                sb.append("      when.equals:");
                sb.append(GlobalConfig.LINE_BREAK);
                sb.append("        fields.type: \"log\"");
                sb.append(GlobalConfig.LINE_BREAK);
                break;
            case k8s:
                sb.append("    - pipeline: k8s-docker-app-name");
                sb.append(GlobalConfig.LINE_BREAK);
                sb.append("      when.equals:");
                sb.append(GlobalConfig.LINE_BREAK);
                sb.append("        fields.type: \"k8s\"");
                sb.append(GlobalConfig.LINE_BREAK);
                break;
            default:
                throw new IllegalArgumentException(" FileBeatTypeConfig type Invalid !");
        }
        return sb.toString();
    }

    /**
     * 获取processor的yml
     *
     * @return
     */
    private String getProcessorsYml(FileBeatConfig fileBeatConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append("processors:");
        sb.append(GlobalConfig.LINE_BREAK);
        if (fileBeatConfig.isAddHostMetadata()) {
            sb.append("- add_host_metadata:");
            sb.append(GlobalConfig.LINE_BREAK);
            if (fileBeatConfig.isAddNetInfo()) {
                sb.append("   netinfo.enabled: true");
                sb.append(GlobalConfig.LINE_BREAK);
                sb.append("- drop_fields:");
                sb.append(GlobalConfig.LINE_BREAK);
                sb.append("   fields: [\"host.mac\"]");
                sb.append(GlobalConfig.LINE_BREAK);
            } else {
                sb.append("   netinfo.enabled: false");
                sb.append(GlobalConfig.LINE_BREAK);
            }
        }

        switch (fileBeatConfig.getFileBeatTypeConfig().getType()) {
            case docker:
                sb.append("- add_docker_metadata: ~");
                sb.append(GlobalConfig.LINE_BREAK);
                break;
            case log:
                break;
            case k8s:
                sb.append("- add_docker_metadata: ~");
                sb.append(GlobalConfig.LINE_BREAK);
                //sb.append("- add_kubernetes_metadata: ");
                //sb.append(LINE_BREAK);
                //sb.append("   in_cluster: false");
                //sb.append(LINE_BREAK);
                //if (fileBeatTypeConfig.isMasterNode()) {
                //    sb.append("   kube_config: /etc/kubernetes/kubectl.kubeconfig");
                //    sb.append(LINE_BREAK);
                //} else {
                //    sb.append("   kube_config: /etc/kubernetes/kube-proxy.kubeconfig");
                //    sb.append(LINE_BREAK);
                //}
                break;
            default:
                throw new IllegalArgumentException(" FileBeatTypeConfig type Invalid !");
        }

        //临时添加 drop_event
        //sb.append("- drop_event:");
        //sb.append(GlobalConfig.LINE_BREAK);
        //sb.append("    when:");
        //sb.append(GlobalConfig.LINE_BREAK);
        //sb.append("       regexp:");
        //sb.append(GlobalConfig.LINE_BREAK);
        //sb.append("           docker.container.labels.io.kubernetes.pod.name: \"^notice-prod-*\"");
        //sb.append(GlobalConfig.LINE_BREAK);
        return sb.toString();
    }

    private String esHostsToString(List<String> list, String separator) {
        String result = "";
        String doubleQuotationMark = "\"";
        if (list == null || list.size() == 0) {
            return result;
        }
        for (int i = 0; i < list.size(); i++) {
            if (StringUtils.isEmpty(result)) {
                result = String.format("%s%s%s", doubleQuotationMark, list.get(i), doubleQuotationMark);
                continue;
            }
            result = result + separator + String.format("%s%s%s", doubleQuotationMark, list.get(i), doubleQuotationMark);
        }
        return result;
    }

    /**
     * 获取filebeat 健康信息
     *
     * @return
     */
    private String getHttpYml() {
        StringBuilder sb = new StringBuilder();

        sb.append("http.enabled: true");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("http.host: 0.0.0.0");
        sb.append(GlobalConfig.LINE_BREAK);
        sb.append("http.port: 5066");
        sb.append(GlobalConfig.LINE_BREAK);

        return sb.toString();
    }

    private String getLoggingYml() {
        StringBuilder sb = new StringBuilder();
        sb.append("logging.level: error");
        sb.append(GlobalConfig.LINE_BREAK);
        return sb.toString();
    }
}
